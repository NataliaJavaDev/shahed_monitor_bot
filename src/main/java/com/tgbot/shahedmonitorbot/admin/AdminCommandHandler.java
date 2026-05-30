package com.tgbot.shahedmonitorbot.admin;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;

@Service
public class AdminCommandHandler {

    private final KeywordAdminService keywordAdminService;
    private final TelegramSenderService senderService;
    private final AdminSessionService sessionService;
    private final AdminAccessService accessService;
    private final AdminMenuService menuService;
    private final ManualAlertService manualAlertService;

    public AdminCommandHandler(
            KeywordAdminService keywordAdminService,
            TelegramSenderService senderService,
            AdminSessionService sessionService,
            AdminAccessService accessService,
            AdminMenuService menuService,
            ManualAlertService manualAlertService
    ) {
        this.keywordAdminService = keywordAdminService;
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
        this.manualAlertService = manualAlertService;
    }

    public void handle(Long userId, String chatId, String text) {

        if (!accessService.isAdmin(userId)) {
            senderService.sendToChat(
                    chatId,
                    "У вас немає доступу до адмін-команд."
            );
            return;
        }

        if (text == null || text.isBlank()) {
            return;
        }

        text = normalizeCommand(text);

        AdminSessionState state = sessionService.getState(userId);

        if (state == AdminSessionState.WAITING_FOR_NEW_KEYWORD) {
            addKeyword(userId, chatId, text);
            return;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_KEYWORD) {
            removeKeyword(userId, chatId, text);
            return;
        }

        if (text.equals("/admin")) {
    senderService.sendToChatWithReplyKeyboard(
            chatId,
            menuService.mainMenuText(),
            menuService.mainReplyKeyboard()
    );
    return;
}

        if (text.equals("/keywords")) {
            sendKeywords(chatId);
            return;
        }

        if (text.equals("/add_keyword")) {
            sessionService.setState(
                    userId,
                    AdminSessionState.WAITING_FOR_NEW_KEYWORD
            );

            senderService.sendToChat(
                    chatId,
                    "Надішліть ключове слово, яке потрібно додати."
            );

            return;
        }

        if (text.startsWith("/add_keyword ")) {
            String keyword = text.replaceFirst("/add_keyword\\s+", "");
            addKeyword(userId, chatId, keyword);
            return;
        }

        if (text.equals("/remove_keyword")) {
            sessionService.setState(
                    userId,
                    AdminSessionState.WAITING_FOR_REMOVE_KEYWORD
            );

            senderService.sendToChat(
                    chatId,
                    "Надішліть ключове слово, яке потрібно видалити."
            );

            return;
        }

        if (text.startsWith("/remove_keyword ")) {
            String keyword = text.replaceFirst("/remove_keyword\\s+", "");
            removeKeyword(userId, chatId, keyword);
            return;
        }


if (text.equals("🔑 Ключові слова")) {
    senderService.sendToChatWithReplyKeyboard(
            chatId,
            "🔑 Ключові слова\n\nОберіть дію:",
            menuService.keywordsReplyKeyboard()
    );
    return;
}

if (text.equals("📋 Показати ключові слова")) {
    sendKeywords(chatId);
    return;
}

if (text.equals("➕ Додати ключове слово")) {
    sessionService.setState(
            userId,
            AdminSessionState.WAITING_FOR_NEW_KEYWORD
    );

    senderService.sendToChat(
            chatId,
            "Надішліть ключове слово, яке потрібно додати."
    );
    return;
}

if (text.equals("➖ Видалити ключове слово")) {
    sessionService.setState(
            userId,
            AdminSessionState.WAITING_FOR_REMOVE_KEYWORD
    );

    senderService.sendToChat(
            chatId,
            "Надішліть ключове слово, яке потрібно видалити."
    );
    return;
}

if (text.equals("🚨 Керування тривогами")) {
    senderService.sendToChatWithReplyKeyboard(
            chatId,
            "🚨 Керування тривогами\n\nОберіть тип сповіщення:",
            menuService.alertReplyKeyboard()
    );
    return;
}

if (text.equals("🚨 Тривога")) {
    manualAlertService.sendAlert(ManualAlertType.ALERT);

    senderService.sendToChat(
            chatId,
            "✅ Сповіщення про тривогу відправлено."
    );
    return;
}

if (text.equals("⚠️ Підвищена небезпека")) {
    manualAlertService.sendAlert(ManualAlertType.HIGH_RISK);

    senderService.sendToChat(
            chatId,
            "✅ Сповіщення про підвищену небезпеку відправлено."
    );
    return;
}

if (text.equals("✅ Відбій")) {
    manualAlertService.sendAlert(ManualAlertType.ALL_CLEAR);

    senderService.sendToChat(
            chatId,
            "✅ Сповіщення про відбій відправлено."
    );
    return;
}

if (text.equals("⬅️ Назад")) {
    senderService.sendToChatWithReplyKeyboard(
            chatId,
            menuService.mainMenuText(),
            menuService.mainReplyKeyboard()
    );
    return;
}

        senderService.sendToChat(
                chatId,
                "Невідома команда. Напишіть /admin"
        );
    }

    private String normalizeCommand(String text) {
        return text.replace("@bc_shahed_monitor_bot", "")
                .trim();
    }

    private void sendKeywords(String chatId) {
        String keywords = String.join(
                "\n",
                keywordAdminService.getKeywords()
        );

        if (keywords.isBlank()) {
            senderService.sendToChat(
                    chatId,
                    "Список ключових слів порожній."
            );
            return;
        }

        senderService.sendToChat(
                chatId,
                """
                Поточні ключові слова:
                
                %s
                """.formatted(keywords)
        );
    }

    private void addKeyword(Long userId, String chatId, String keyword) {
        boolean added = keywordAdminService.addKeyword(keyword);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(
                    chatId,
                    "Ключове слово додано: " + keyword
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    "Не вдалося додати ключове слово."
            );
        }
    }

    private void removeKeyword(Long userId, String chatId, String keyword) {
        boolean removed = keywordAdminService.removeKeyword(keyword);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(
                    chatId,
                    "Ключове слово видалено: " + keyword
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    "Ключове слово не знайдено."
            );
        }
    }
}