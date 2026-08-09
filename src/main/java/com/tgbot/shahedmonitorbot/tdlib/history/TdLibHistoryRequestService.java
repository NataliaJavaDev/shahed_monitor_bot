package com.tgbot.shahedmonitorbot.tdlib.history;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.tdlib.TdLibClientService;
import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class TdLibHistoryRequestService {

    private static final String EXTRA_PREFIX = "HISTORY_REQUEST:";
    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");

    private final TdLibClientService tdLibClientService;
    private final ObjectMapper objectMapper;
    private final AlertDeliveryService alertDeliveryService;

    private final Map<String, HistoryRequest> activeRequests =
            new ConcurrentHashMap<>();

    public TdLibHistoryRequestService(
            TdLibClientService tdLibClientService,
            ObjectMapper objectMapper,
            AlertDeliveryService alertDeliveryService
    ) {
        this.tdLibClientService = tdLibClientService;
        this.objectMapper = objectMapper;
        this.alertDeliveryService = alertDeliveryService;
    }

    public CompletableFuture<List<TdHistoryMessage>> requestHistory(
            String chatId,
            Duration lookback
    ) {

        String requestId = UUID.randomUUID().toString();

        HistoryRequest request = new HistoryRequest(
                requestId,
                chatId,
                LocalDateTime.now(KYIV_ZONE).minus(lookback)
        );

        activeRequests.put(requestId, request);

        alertDeliveryService.send("""
DEBUG

START
requestId: %s
chat: %s
""".formatted(
        requestId,
        chatId
));

        requestHistoryPage(request, 0);

        CompletableFuture<List<TdHistoryMessage>> future =
                request.getFuture();

            future
                .orTimeout(60, TimeUnit.SECONDS)
                .whenComplete((r, ex) -> {
                    if (ex != null) {

            alertDeliveryService.send("""
DEBUG

TIMEOUT
requestId: %s
chat: %s
""".formatted(
                    requestId,
                    chatId
            ));

            activeRequests.remove(requestId);
        }
                });

        return future;
    }

    public CompletableFuture<Map<String, List<TdHistoryMessage>>> requestHistory(
        Collection<String> chatIds,
        Duration lookback
) {

    CompletableFuture<Map<String, List<TdHistoryMessage>>> future =
            CompletableFuture.completedFuture(new HashMap<>());

    for (String chatId : chatIds) {

        future = future.thenCompose(result ->

                requestHistory(chatId, lookback)
                        .thenApply(messages -> {

                            result.put(chatId, messages);

                            return result;
                        })
        );
    }

    return future;
}

    // public CompletableFuture<Map<String, List<TdHistoryMessage>>> requestHistory(
    //         Collection<String> chatIds,
    //         Duration lookback
    // ) {

    //     Map<String, CompletableFuture<List<TdHistoryMessage>>> futures =
    //             new HashMap<>();

    //     for (String chatId : chatIds) {
    //         futures.put(chatId, requestHistory(chatId, lookback));
    //     }

    //     CompletableFuture<?>[] array =
    //             futures.values().toArray(new CompletableFuture[0]);

    //     return CompletableFuture
    //             .allOf(array)
    //             .thenApply(v -> {

    //                 Map<String, List<TdHistoryMessage>> result =
    //                         new HashMap<>();

    //                 futures.forEach((chatId, future) ->
    //                         result.put(chatId, future.join()));

    //                 return result;
    //             });
    // }

    public void handle(String update) {

    JsonNode root = null;
    HistoryRequest request = null;

    try {

        if (update == null
                || !update.contains("\"@type\":\"messages\"")) {
            return;
        }

        root = objectMapper.readTree(update);

        String extra = root.path("@extra").asText("");

        if (!extra.startsWith(EXTRA_PREFIX)) {
            return;
        }

        String requestId =
                extra.substring(EXTRA_PREFIX.length());

        alertDeliveryService.send("""
DEBUG

HANDLE
requestId: %s
""".formatted(requestId));

alertDeliveryService.send("A");

        request = activeRequests.get(requestId);

        if (request == null) {
            return;
        }

        alertDeliveryService.send("B");

        JsonNode messages = root.path("messages");

        alertDeliveryService.send("C");

        if (!messages.isArray()
                || messages.isEmpty()) {

            finishRequest(request);

            return;
        }

        alertDeliveryService.send("D");

        boolean reachedFromTime = false;

        alertDeliveryService.send(
                "DEBUG\n\nBefore loop: " + messages.size()
        );

        for (JsonNode message : messages) {

            long messageId =
                    message.path("id").asLong();

            if (request.getOldestMessageId() == 0
                    || messageId < request.getOldestMessageId()) {

                request.setOldestMessageId(messageId);
            }

            LocalDateTime messageTime =
                    LocalDateTime.ofInstant(
                            Instant.ofEpochSecond(
                                    message.path("date").asLong()
                            ),
                            KYIV_ZONE
                    );

            if (messageTime.isBefore(request.getFrom())) {

                reachedFromTime = true;

                break;
            }

            String text = extractText(message);

            if (text.isBlank()) {
                continue;
            }

            request.getMessages().add(
                    new TdHistoryMessage(
                            messageId,
                            messageTime,
                            text
                    )
            );
        }

        alertDeliveryService.send(
                "DEBUG\n\nAfter loop"
        );

        if (reachedFromTime) {

            finishRequest(request);

            return;
        }

        requestHistoryPage(
                request,
                request.getOldestMessageId()
        );

    } catch (Exception e) {

        alertDeliveryService.send("""
DEBUG

HANDLE EXCEPTION

%s
""".formatted(e));

        failRequest(request, e);
    }
}

//     public void handle(String update) {

//         if (update == null
//                 || !update.contains("\"@type\":\"messages\"")) {
//             return;
//         }

//         try {

//             JsonNode root = objectMapper.readTree(update);

//             String extra = root.path("@extra").asText("");

//             if (!extra.startsWith(EXTRA_PREFIX)) {
//                 return;
//             }

//             String requestId =
//                     extra.substring(EXTRA_PREFIX.length());

//             alertDeliveryService.send("""
// DEBUG

// HANDLE
// requestId: %s
// """.formatted(requestId));

//             HistoryRequest request =
//                     activeRequests.get(requestId);

//             if (request == null) {
//                 return;
//             }

//             JsonNode messages = root.path("messages");

//             if (!messages.isArray()
//                     || messages.isEmpty()) {

//                 finishRequest(request);

//                 return;
//             }

//             boolean reachedFromTime = false;

//             alertDeliveryService.send(
//         "DEBUG\n\nBefore loop: " + messages.size()
// );

//             for (JsonNode message : messages) {

//                 long messageId =
//                         message.path("id").asLong();

//                 if (request.getOldestMessageId() == 0
//                         || messageId < request.getOldestMessageId()) {

//                     request.setOldestMessageId(messageId);
//                 }

//                 LocalDateTime messageTime =
//                         LocalDateTime.ofInstant(
//                                 Instant.ofEpochSecond(
//                                         message.path("date").asLong()
//                                 ),
//                                 KYIV_ZONE
//                         );

//                 if (messageTime.isBefore(request.getFrom())) {

//                     reachedFromTime = true;

//                     break;
//                 }

//                 String text = extractText(message);

//                 if (text.isBlank()) {
//                     continue;
//                 }

//                 request.getMessages().add(
//                         new TdHistoryMessage(
//                                 messageId,
//                                 messageTime,
//                                 text
//                         )
//                 );
//             }

//             alertDeliveryService.send(
//         "DEBUG\n\nAfter loop"
// );

//             if (reachedFromTime) {

//                 finishRequest(request);

//                 return;
//             }

//             requestHistoryPage(
//                     request,
//                     request.getOldestMessageId()
//             );

//         } catch (Exception e) {

//             System.out.println(
//                 "Failed to process history response: "
//                         + e.getMessage()
//             );

//             e.printStackTrace();

//         }
//     }

    private void requestHistoryPage(
        HistoryRequest request,
        long fromMessageId
    ) {

        // int offset = fromMessageId == 0 ? 0 : 1;

        int offset = 0;

        alertDeliveryService.send("""
DEBUG

PAGE

chat: %s
fromMessageId: %d
offset: %d
""".formatted(
        request.getChatId(),
        fromMessageId,
        offset
));

        tdLibClientService.send("""
                {
                "@type":"getChatHistory",
                "@extra":"%s%s",
                "chat_id":%s,
                "from_message_id":%d,
                "offset":%d,
                "limit":100,
                "only_local":false
                }
                """.formatted(
                EXTRA_PREFIX,
                request.getRequestId(),
                request.getChatId(),
                fromMessageId,
                offset
        ));
    }

    private void finishRequest(HistoryRequest request) {

        alertDeliveryService.send("""
DEBUG

FINISH
Chat: %s
Messages: %d
""".formatted(
        request.getChatId(),
        request.getMessages().size()
));

        activeRequests.remove(request.getRequestId());

        request.getFuture().complete(List.copyOf(request.getMessages()));
    }

    private void failRequest(
            HistoryRequest request,
            Exception e
    ) {

        if (request == null) {

            alertDeliveryService.send("""
DEBUG

FAIL REQUEST

request == null

%s
""".formatted(e));

            return;
        }

        activeRequests.remove(request.getRequestId());

        request.getFuture()
            .completeExceptionally(e);
    }

    private String extractText(JsonNode message) {

        JsonNode content = message.path("content");

        String contentType =
                content.path("@type").asText();

        if ("messageText".equals(contentType)) {

            return content
                    .path("text")
                    .path("text")
                    .asText("");

        }

        if ("messagePhoto".equals(contentType)) {

            return content
                    .path("caption")
                    .path("text")
                    .asText("");

        }

        return "";
    }
}