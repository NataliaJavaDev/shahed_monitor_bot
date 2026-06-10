package com.tgbot.shahedmonitorbot.admin.command;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.alertapi.formatter.ApiAlertStatusFormatter;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
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
    private final AirAlertApiService airAlertApiService;
    private final ApiAlertStatusFormatter apiAlertStatusFormatter;
    private final MonitoringStateService monitoringStateService;
    private final MonitoredSourceService monitoredSourceService;

    public AdminCommandHandler(
            KeywordAdminService keywordAdminService,
            TelegramSenderService senderService,
            AdminSessionService sessionService,
            AdminAccessService accessService,
            AdminMenuService menuService,
            ManualAlertService manualAlertService,
            AirAlertApiService airAlertApiService,
            ApiAlertStatusFormatter apiAlertStatusFormatter,
            MonitoringStateService monitoringStateService,
            MonitoredSourceService monitoredSourceService
    ) {
        this.keywordAdminService = keywordAdminService;
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
        this.manualAlertService = manualAlertService;
        this.airAlertApiService = airAlertApiService;
        this.apiAlertStatusFormatter = apiAlertStatusFormatter;
        this.monitoringStateService = monitoringStateService;
        this.monitoredSourceService = monitoredSourceService;
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

        boolean isCommand = AdminCommand.fromText(text) != null;
        boolean isButton = AdminButton.fromText(text) != null;

        if (!isCommand && !isButton && handleSession(userId, chatId, text)) {
            return;
        }

        if (handleCommand(userId, chatId, text)) {
            return;
        }

        if (handleButton(userId, chatId, text)) {
            return;
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

    private boolean handleCommand(Long userId, String chatId, String text) {

        AdminCommand command = AdminCommand.fromText(text);

        if (command == null) {
            return false;
        }

        switch (command) {

            case START:
                senderService.sendToChat(
                    chatId,
                    AdminMessage.WELCOME_MESSAGE.text()
                );
                return true;

            case ADMIN:
                sendMainMenu(chatId);
                return true;

            case KEYWORDS:
                sendKeywords(chatId);
                return true;

            case ADD_KEYWORD:
                requestAddKeyword(userId, chatId);
                return true;

            case REMOVE_KEYWORD:
                requestRemoveKeyword(userId, chatId);
                return true;
            }

            return false;
        }

    private void requestKeyword(Long userId, String chatId, AdminSessionState state, AdminMessage message) {
        
        sessionService.setState(userId, state);
        senderService.sendToChat(chatId, message.text());
    }

    private void requestAddKeyword(Long userId, String chatId) {

        requestKeyword(
            userId,
            chatId,
            AdminSessionState.WAITING_FOR_NEW_KEYWORD,
            AdminMessage.ADD_KEYWORD_REQUEST
        );
    }

    private void requestRemoveKeyword(Long userId, String chatId) {

        requestKeyword(
            userId,
            chatId,
            AdminSessionState.WAITING_FOR_REMOVE_KEYWORD,
            AdminMessage.REMOVE_KEYWORD_REQUEST
        );
    }

    private void sendManualAlert(String chatId, ManualAlertType type, AdminMessage successMessage) {
        
        manualAlertService.sendAlert(type);
        senderService.sendToChat(chatId, successMessage.text());
    }

    private void sendMainMenu(String chatId) {
        
        senderService.sendToChatWithReplyKeyboard(
            chatId,
            menuService.mainMenuText(),
            menuService.mainReplyKeyboard()
        );
    }

    private void sendComingSoon(String chatId) {

        senderService.sendToChat(
            chatId,
            AdminMessage.COMING_SOON.text()
        );
    }

    private boolean handleButton(Long userId, String chatId, String text) {

        AdminButton button = AdminButton.fromText(text);

        if (button == null) {
            return false;
        }

        switch (button) {

            case KEYWORDS:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                    AdminMessage.KEYWORDS_MENU_TITLE.text(),
                    menuService.keywordsReplyKeyboard()
                );
                return true;

            case SHOW_KEYWORDS:
                sendKeywords(chatId);
                return true;

            case ADD_KEYWORD:
                requestAddKeyword(userId, chatId);
                return true;
                
            case REMOVE_KEYWORD:
                requestRemoveKeyword(userId, chatId);
                return true;

            case ALERTS:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                    AdminMessage.ALERT_MENU_TITLE.text(),
                    menuService.alertReplyKeyboard()
                );
                return true;

            case ALERT:
                sendManualAlert(chatId, ManualAlertType.ALERT, AdminMessage.ALERT_SENT);
                return true;

            case HIGH_RISK:
                sendManualAlert(chatId, ManualAlertType.HIGH_RISK, AdminMessage.HIGH_RISK_SENT);
                return true;

            case ALL_CLEAR:
                sendManualAlert(chatId, ManualAlertType.ALL_CLEAR, AdminMessage.ALL_CLEAR_SENT);
                return true;

            case BACK:
                sendMainMenu(chatId);
                return true;

            case STATUS:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                    "📊 Статус\n\nОберіть дію:",
                    menuService.statusReplyKeyboard()
                );
                return true;

            case ALERT_STATUS:
                sendAlertStatus(chatId);
                return true;

            case BOT_STATUS:
                sendBotStatus(chatId);
                return true;

            case SETTINGS:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                    "⚙️ Налаштування\n\nОберіть дію:",
                    menuService.settingsReplyKeyboard()
                );
                return true;

            case API_CONTROL:
                monitoringStateService.toggleApiControl();

                senderService.sendToChat(
                    chatId,
                    "🔌 API-керування: "
                    + monitoringStateService.getApiControlStatus()
                );
                return true;

            case SOURCES:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                "📡 Джерела моніторингу\n\nОберіть дію:",
                menuService.sourcesReplyKeyboard()
                );
                return true;

            case SHOW_SOURCES:
                sendSources(chatId);
                return true;

            case ADD_SOURCE:
            case REMOVE_SOURCE:
                sendComingSoon(chatId);
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

    private void sendBotStatus(String chatId) {

        String message = """
                🤖 Статус бота
                
                API-керування моніторингом: %s
                Моніторинг повідомлень: %s
                """.formatted(
                monitoringStateService.isApiControlEnabled() ? "увімкнене" : "вимкнене",
                monitoringStateService.isMonitoringEnabled() ? "увімкнений" : "вимкнений"
        );

        senderService.sendToChat(chatId, message);
    }

    private void sendAlertStatus(String chatId) {
        senderService.sendToChat(
                chatId,
                apiAlertStatusFormatter.format(
                        airAlertApiService.getLastStatus()
                )
        );
    }

    private void sendSources(String chatId) {

        var sources = monitoredSourceService.getAllSources();

        if (sources.isEmpty()) {
            senderService.sendToChat(
                    chatId,
                    "Список джерел моніторингу порожній."
            );
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("📡 Джерела моніторингу:\n\n");

        for (var source : sources) {
            builder.append(source.active() ? "✅ " : "⛔ ")
                    .append(source.title())
                    .append("\n")
                    .append("ID: ")
                    .append(source.chatId())
                    .append("\n\n");
        }

        senderService.sendToChat(chatId, builder.toString());
    }

}