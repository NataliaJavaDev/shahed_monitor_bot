package com.tgbot.shahedmonitorbot.admin;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class AdminCommandHandler {

    private final KeywordAdminService keywordAdminService;
    private final TelegramSenderService senderService;
    private final AdminSessionService sessionService;
    private final AdminAccessService accessService;
    private final AdminMenuService menuService;

    public AdminCommandHandler(
            KeywordAdminService keywordAdminService,
            TelegramSenderService senderService,
            AdminSessionService sessionService,
            AdminAccessService accessService,
            AdminMenuService menuService
    ) {
        this.keywordAdminService = keywordAdminService;
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
    }

    public void handle(Long userId, String text) {
        if (!accessService.isAdmin(userId)) {
            senderService.send("У вас немає доступу до адмін-команд.");
            return;
        }

        if (text == null || text.isBlank()) {
            return;
        }

        AdminSessionState state = sessionService.getState(userId);

        if (state == AdminSessionState.WAITING_FOR_NEW_KEYWORD) {
            addKeyword(userId, text);
            return;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_KEYWORD) {
            removeKeyword(userId, text);
            return;
        }

        if (text.equals("/admin")) {
            senderService.send(menuService.mainMenuText());
            return;
        }

        if (text.equals("/keywords")) {
            sendKeywords();
            return;
        }

        if (text.equals("/add_keyword")) {
            sessionService.setState(userId, AdminSessionState.WAITING_FOR_NEW_KEYWORD);
            senderService.send("Надішліть ключове слово, яке потрібно додати.");
            return;
        }

        if (text.startsWith("/add_keyword ")) {
            String keyword = text.replaceFirst("/add_keyword\\s+", "");
            addKeyword(userId, keyword);
            return;
        }

        if (text.equals("/remove_keyword")) {
            sessionService.setState(userId, AdminSessionState.WAITING_FOR_REMOVE_KEYWORD);
            senderService.send("Надішліть ключове слово, яке потрібно видалити.");
            return;
        }

        if (text.startsWith("/remove_keyword ")) {
            String keyword = text.replaceFirst("/remove_keyword\\s+", "");
            removeKeyword(userId, keyword);
            return;
        }

        senderService.send("Невідома команда. Напишіть /admin");
    }

    private void sendKeywords() {
        String keywords = String.join("\n", keywordAdminService.getKeywords());

        if (keywords.isBlank()) {
            senderService.send("Список ключових слів порожній.");
            return;
        }

        senderService.send("""
                Поточні ключові слова:
                
                %s
                """.formatted(keywords));
    }

    private void addKeyword(Long userId, String keyword) {
        boolean added = keywordAdminService.addKeyword(keyword);
        sessionService.reset(userId);

        if (added) {
            senderService.send("Ключове слово додано: " + keyword);
        } else {
            senderService.send("Не вдалося додати ключове слово. Можливо, воно вже існує.");
        }
    }

    private void removeKeyword(Long userId, String keyword) {
        boolean removed = keywordAdminService.removeKeyword(keyword);
        sessionService.reset(userId);

        if (removed) {
            senderService.send("Ключове слово видалено: " + keyword);
        } else {
            senderService.send("Ключове слово не знайдено: " + keyword);
        }
    }
}