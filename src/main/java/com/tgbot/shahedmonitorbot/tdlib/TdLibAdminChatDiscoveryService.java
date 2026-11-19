
package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class TdLibAdminChatDiscoveryService {

    private static final Logger log = LoggerFactory.getLogger(TdLibAdminChatDiscoveryService.class);

    private static final String GET_ME_EXTRA = "TDLIB_ADMIN_DISCOVERY_GET_ME";
    private static final String GET_CHAT_EXTRA = "TDLIB_ADMIN_DISCOVERY_GET_CHAT";
    private static final String MEMBER_EXTRA_PREFIX = "TDLIB_ADMIN_DISCOVERY_MEMBER:";

    private final TdLibClientService tdLibClientService;
    private final ObjectMapper objectMapper;

    private final AtomicBoolean started = new AtomicBoolean(false);

    private final Map<Long, String> pendingChats = new ConcurrentHashMap<>();
    private final Set<Long> requestedMemberChecks = ConcurrentHashMap.newKeySet();

    private volatile long currentUserId;

    public TdLibAdminChatDiscoveryService(
        TdLibClientService tdLibClientService,
        ObjectMapper objectMapper
    ) {
        this.tdLibClientService = tdLibClientService;
        this.objectMapper = objectMapper;
    }

    public void start() {
        if (!started.compareAndSet(false, true)) {
            return;
        }

        log.info("Starting TDLib admin chat discovery");

        tdLibClientService.send("""
                {
                    "@type": "getMe",
                    "@extra": "%s"
                }
            """.formatted(GET_ME_EXTRA));
    }

    public void handle(String update) {
        try {
            JsonNode root = objectMapper.readTree(update);

            String type = root.path("@type").asText();
            String extra = root.path("@extra").asText();

            if (GET_ME_EXTRA.equals(extra) && "user".equals(type)) {
                handleCurrentUser(root);
                return;
            }

            if (GET_CHAT_EXTRA.equals(extra) && "chat".equals(type)) {
                handleChat(root);
                return;
            }

            if (extra.startsWith(MEMBER_EXTRA_PREFIX)) {
                handleMemberResult(root, type, extra);
            }

        } catch (Exception e) {
            log.error("Failed to process TDLib admin discovery update", e);
        }
    }

    private void handleCurrentUser(JsonNode user) {
        currentUserId = user.path("id").asLong();

        log.info("TDLib authorized user ID: {}", currentUserId);

        for (Map.Entry<Long, String> entry : pendingChats.entrySet()) {
            requestMemberStatus(entry.getKey());
        }
    }

    private void handleChat(JsonNode chat) {
        long chatId = chat.path("id").asLong();
        String title = chat.path("title").asText("Без назви");

        JsonNode chatType = chat.path("type");

        if (!"chatTypeSupergroup".equals(
                chatType.path("@type").asText())) {
            return;
        }

        // Відкидаємо канали. Нам потрібні саме супергрупи.
        if (chatType.path("is_channel").asBoolean(true)) {
            return;
        }

        pendingChats.put(chatId, title);
        log.debug("Found supergroup: {} (ID: {})", title, chatId);

        if (currentUserId != 0) {
            requestMemberStatus(chatId);
        }
    }

    private void requestMemberStatus(long chatId) {
        if (currentUserId == 0) {
            return;
        }

        if (!requestedMemberChecks.add(chatId)) {
            return;
        }

        tdLibClientService.send("""
                {
                    "@type": "getChatMember",
                    "chat_id": %d,
                    "member_id": {
                        "@type": "messageSenderUser",
                        "user_id": %d
                    },
                    "@extra": "%s%d"
                }
            """.formatted(
            chatId,
            currentUserId,
            MEMBER_EXTRA_PREFIX,
            chatId
        ));
    }

    private void handleMemberResult(
        JsonNode result,
        String type,
        String extra
    ) {
        long chatId;

        try {
            chatId = Long.parseLong(extra.substring(MEMBER_EXTRA_PREFIX.length()));
        } catch (NumberFormatException e) {
            log.warn("Invalid member discovery response ID: {}", extra);
            return;
        }

        String title = pendingChats.getOrDefault(chatId, "Без назви");

        if ("error".equals(type)) {
            log.warn(
                "Could not check TDLib membership in '{}' ({}): {}",
                title,
                chatId,
                result.path("message").asText("Unknown error")
            );
            return;
        }

        if (!"chatMember".equals(type)) {
            return;
        }

        String status = result.path("status").path("@type").asText();

        if ("chatMemberStatusCreator".equals(status)
                || "chatMemberStatusAdministrator".equals(status)
                || "chatMemberStatusEditor".equals(status)) {

            log.info("TDLIB ADMIN GROUP FOUND: title='{}', chat_id={}, status={}",
                title,
                chatId,
                status
            );
        } else {
            log.debug("TDLib user is not an administrator in '{}': status={}", title, status);
        }
    }
}
