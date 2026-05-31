package com.tgbot.shahedmonitorbot.admin.command;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.admin.menu.AdminMenuService;
import com.tgbot.shahedmonitorbot.admin.service.AdminAccessService;
import com.tgbot.shahedmonitorbot.admin.service.AdminSessionService;
import com.tgbot.shahedmonitorbot.admin.service.KeywordAdminService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;
import com.tgbot.shahedmonitorbot.admin.enums.*;


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
                    AdminMessage.NO_ACCESS.text()
            );
            return;
        }

        if (text == null || text.isBlank()) {
            return;
        }

        text = normalizeCommand(text);

        if (handleSession(userId, chatId, text)) {
            return;
        }

        AdminCommand command = AdminCommand.fromText(text);

        if (command != null) {

            switch (command) {

                case START:
                    senderService.sendToChat(
                        chatId,
                        AdminMessage.WELCOME_MESSAGE.text()
                    );
                    return;

                case ADMIN:
                    senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        menuService.mainMenuText(),
                        menuService.mainReplyKeyboard()
                    );
                    return;

                case KEYWORDS:
                    sendKeywords(chatId);
                    return;

                case ADD_KEYWORD:
                    if (AdminCommand.ADD_KEYWORD.startsWith(text)) {
                        String keyword = text
                        .replaceFirst("/add_keyword\\s+", "");
                        addKeyword(userId, chatId, keyword);
                        return;
                    }

                    sessionService.setState(
                        userId,
                        AdminSessionState.WAITING_FOR_NEW_KEYWORD
                    );

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.ADD_KEYWORD_REQUEST.text()
                    );
                    return;

                case REMOVE_KEYWORD:
                    if (AdminCommand.REMOVE_KEYWORD.startsWith(text)) {
                        String keyword = text.replaceFirst("/remove_keyword\\s+", "");
                        removeKeyword(userId, chatId, keyword);
                        return;
                    }

                    sessionService.setState(
                        userId,
                        AdminSessionState.WAITING_FOR_REMOVE_KEYWORD
                    );

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.REMOVE_KEYWORD_REQUEST.text()
                    );
                    return;
            }
        }

        AdminButton button = AdminButton.fromText(text);

        if (button != null) {

            switch (button) {

                case KEYWORDS:
                    senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        AdminMessage.KEYWORDS_MENU_TITLE.text(),
                        menuService.keywordsReplyKeyboard()
                    );
                    return;

                case SHOW_KEYWORDS:
                    sendKeywords(chatId);
                    return;

                case ADD_KEYWORD:
                    sessionService.setState(
                        userId,
                        AdminSessionState.WAITING_FOR_NEW_KEYWORD
                    );

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.ADD_KEYWORD_REQUEST.text()
                    );
                    return;
                
                case REMOVE_KEYWORD:
                    sessionService.setState(
                        userId,
                        AdminSessionState.WAITING_FOR_REMOVE_KEYWORD
                    );

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.REMOVE_KEYWORD_REQUEST.text()
                    );
                    return;

                case ALERTS:
                    senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        AdminMessage.ALERT_MENU_TITLE.text(),
                        menuService.alertReplyKeyboard()
                    );
                    return;

                case ALERT:
                    manualAlertService.sendAlert(ManualAlertType.ALERT);

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.ALERT_SENT.text()
                    );
                    return;

                case HIGH_RISK:
                    manualAlertService.sendAlert(ManualAlertType.HIGH_RISK);

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.HIGH_RISK_SENT.text()
                    );
                    return;

                case ALL_CLEAR:
                    manualAlertService.sendAlert(ManualAlertType.ALL_CLEAR);

                    senderService.sendToChat(
                        chatId,
                        AdminMessage.ALL_CLEAR_SENT.text()
                    );
                    return;

                case BACK:
                    senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        menuService.mainMenuText(),
                        menuService.mainReplyKeyboard()
                    );
                    return;

                case STATUS:
                    senderService.sendToChat(
                        chatId,
                        AdminMessage.COMING_SOON.text()
                    );
                    return;

                case SETTINGS:
                    senderService.sendToChat(
                        chatId,
                        AdminMessage.COMING_SOON.text()
                    );
                    return;
            }
        }

        senderService.sendToChat(
                chatId,
                AdminMessage.UNKNOWN_COMMAND.text()
        );
    }

    private boolean handleSession(Long userId, String chatId, String text) {

        AdminSessionState state = sessionService.getState(userId);

        if (state == AdminSessionState.WAITING_FOR_NEW_KEYWORD) {
            addKeyword(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_KEYWORD) {
            removeKeyword(userId, chatId, text);
            return true;
        }

        return false;
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
                    AdminMessage.EMPTY_KEYWORDS.text()
            );
            return;
        }

        senderService.sendToChat(
                chatId,
                AdminMessage.SHOW_KEYWORDS.format(keywords)
        );
    }

    private void addKeyword(Long userId, String chatId, String keyword) {
        boolean added = keywordAdminService.addKeyword(keyword);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(
                    chatId,
                    AdminMessage.KEYWORD_ADDED.format(keyword)
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    AdminMessage.KEYWORD_ADD_FAILED.text()
            );
        }
    }

    private void removeKeyword(Long userId, String chatId, String keyword) {
        boolean removed = keywordAdminService.removeKeyword(keyword);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(
                    chatId,
                    AdminMessage.KEYWORD_REMOVED.format(keyword)
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    AdminMessage.KEYWORD_NOT_FOUND.text()
            );
        }
    }
}