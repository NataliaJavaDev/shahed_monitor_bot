package com.tgbot.shahedmonitorbot.admin.command;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.alertapi.formatter.ApiAlertStatusFormatter;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.admin.menu.AdminMenuService;
import com.tgbot.shahedmonitorbot.admin.service.AdminAccessService;
import com.tgbot.shahedmonitorbot.admin.service.AdminSessionService;
import com.tgbot.shahedmonitorbot.admin.service.LocationAdminService;
import com.tgbot.shahedmonitorbot.admin.service.TargetAdminService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;
import com.tgbot.shahedmonitorbot.admin.enums.*;


@Service
public class AdminCommandHandler {

    private final TargetAdminService targetAdminService;
    private final LocationAdminService locationAdminService;
    private final TelegramSenderService senderService;
    private final AdminSessionService sessionService;
    private final AdminAccessService accessService;
    private final AdminMenuService menuService;
    private final ManualAlertService manualAlertService;
    private final AirAlertApiService airAlertApiService;
    private final ApiAlertStatusFormatter apiAlertStatusFormatter;
    private final MonitoringStateService monitoringStateService;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;

    public AdminCommandHandler(
            TargetAdminService targetAdminService,
            LocationAdminService locationAdminService,
            TelegramSenderService senderService,
            AdminSessionService sessionService,
            AdminAccessService accessService,
            AdminMenuService menuService,
            ManualAlertService manualAlertService,
            AirAlertApiService airAlertApiService,
            ApiAlertStatusFormatter apiAlertStatusFormatter,
            MonitoringStateService monitoringStateService,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService
    ) {
        this.targetAdminService = targetAdminService;
        this.locationAdminService = locationAdminService;
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
        this.manualAlertService = manualAlertService;
        this.airAlertApiService = airAlertApiService;
        this.apiAlertStatusFormatter = apiAlertStatusFormatter;
        this.monitoringStateService = monitoringStateService;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
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

        if (state == AdminSessionState.WAITING_FOR_NEW_TARGET) {
            addTarget(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_TARGET) {
            removeTarget(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_NEW_LOCATION) {
            addLocation(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_LOCATION) {
            removeLocation(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_NEW_SOURCE_ID) {
            saveSourceId(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_NEW_SOURCE_TITLE) {
            addSource(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_SOURCE) {
            removeSource(userId, chatId, text);
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
        }

        return false;
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
                        "🔑 Ключові слова\n\nОберіть розділ:",
                        menuService.keywordsReplyKeyboard()
                );
                return true;

            case TARGETS:
                senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        AdminMessage.TARGETS_MENU_TITLE.text(),
                        menuService.targetsReplyKeyboard()
                );
                return true;

            case SHOW_TARGETS:
                sendTargets(chatId);
                return true;

            case ADD_TARGET:
                requestAddTarget(userId, chatId);
                return true;

            case REMOVE_TARGET:
                requestRemoveTarget(userId, chatId);
                return true;

            case LOCATIONS:
                senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        AdminMessage.LOCATIONS_MENU_TITLE.text(),
                        menuService.locationsReplyKeyboard()
                );
                return true;

            case SHOW_LOCATIONS:
                sendLocations(chatId);
                return true;

            case ADD_LOCATION:
                requestAddLocation(userId, chatId);
                return true;

            case REMOVE_LOCATION:
                requestRemoveLocation(userId, chatId);
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
                sendUnknownSources(chatId);
                return true;

            case REMOVE_SOURCE:
                requestRemoveSource(userId, chatId);
                return true;
        }
        return false;
    }

    private void requestKeyword(Long userId, String chatId, AdminSessionState state, AdminMessage message) {
        
        sessionService.setState(userId, state);
        senderService.sendToChat(chatId, message.text());
    }

    private void sendTargets(String chatId) {

        String targets = String.join(
                "\n",
                targetAdminService.getTargets()
        );

        if (targets.isBlank()) {
            senderService.sendToChat(chatId, "Список цілей порожній.");
            return;
        }

        senderService.sendToChat(
                chatId,
                "🎯 Цілі моніторингу:\n\n" + targets
        );
    }

    private void sendLocations(String chatId) {
        String locations = String.join(
                "\n",
                locationAdminService.getLocations()
        );

        if (locations.isBlank()) {
            senderService.sendToChat(chatId, "Список локацій порожній.");
            return;
        }

        senderService.sendToChat(
                chatId,
                "📍 Локації моніторингу:\n\n" + locations
        );
    }

    private void addTarget(Long userId, String chatId, String target) {
        boolean added = targetAdminService.addTarget(target);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(chatId, "✅ Ціль додано: " + target);
        } else {
            senderService.sendToChat(chatId, "⚠️ Таку ціль уже додано або значення порожнє.");
        }
    }

    private void removeTarget(Long userId, String chatId, String target) {
        boolean removed = targetAdminService.removeTarget(target);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(chatId, "✅ Ціль видалено: " + target);
        } else {
            senderService.sendToChat(chatId, "⚠️ Таку ціль не знайдено.");
        }
    }

    private void addLocation(Long userId, String chatId, String location) {
        boolean added = locationAdminService.addLocation(location);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(chatId, "✅ Локацію додано: " + location);
        } else {
            senderService.sendToChat(chatId, "⚠️ Таку локацію вже додано або значення порожнє.");
        }
    }

    private void removeLocation(Long userId, String chatId, String location) {
        boolean removed = locationAdminService.removeLocation(location);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(chatId, "✅ Локацію видалено: " + location);
        } else {
            senderService.sendToChat(chatId, "⚠️ Таку локацію не знайдено.");
        }
    }

    private void requestAddTarget(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_NEW_TARGET);
        senderService.sendToChat(chatId, "Введіть ціль для моніторингу:");
    }

    private void requestRemoveTarget(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_REMOVE_TARGET);
        senderService.sendToChat(chatId, "Введіть ціль, яку треба видалити:");
    }

    private void requestAddLocation(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_NEW_LOCATION);
        senderService.sendToChat(chatId, "Введіть локацію для моніторингу:");
    }

    private void requestRemoveLocation(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_REMOVE_LOCATION);
        senderService.sendToChat(chatId, "Введіть локацію, яку треба видалити:");
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

    private String normalizeCommand(String text) {

        return text.replace("@bc_shahed_monitor_bot", "")
                .trim();
    }

    private void sendBotStatus(String chatId) {

        String message = """
                🤖 Статус бота
                
                TDLib-моніторинг каналів: увімкнений
                API-керування моніторингом: %s
                API-режим активного моніторингу: %s
                """.formatted(
                monitoringStateService.isApiControlEnabled() ? "увімкнене" : "вимкнене",
                monitoringStateService.isMonitoringEnabled() ? "активний" : "неактивний"
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

    private void requestAddSource(Long userId, String chatId) {

        sessionService.setState(
                userId,
                AdminSessionState.WAITING_FOR_NEW_SOURCE_ID
        );

        senderService.sendToChat(
                chatId,
                "Введіть chat_id джерела моніторингу:"
        );
    }

    private void requestRemoveSource(Long userId, String chatId) {

        sessionService.setState(
                userId,
                AdminSessionState.WAITING_FOR_REMOVE_SOURCE
        );

        senderService.sendToChat(
                chatId,
                "Введіть chat_id джерела, яке треба видалити:"
        );
    }

    private void saveSourceId(Long userId, String chatId, String sourceId) {

        sessionService.setPendingSourceId(userId, sourceId);
        sessionService.setState(
                userId,
                AdminSessionState.WAITING_FOR_NEW_SOURCE_TITLE
        );

        senderService.sendToChat(
                chatId,
                "Тепер введіть назву джерела:"
        );
    }

    private void addSource(Long userId, String chatId, String title) {

        String sourceId = sessionService.getPendingSourceId(userId);

        boolean added = monitoredSourceService.addSource(sourceId, title);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(
                    chatId,
                    "✅ Джерело додано:\n\n"
                            + title
                            + "\nID: "
                            + sourceId
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    "⚠️ Джерело з таким ID уже існує."
            );
        }
    }

    private void removeSource(Long userId, String chatId, String sourceId) {

        boolean removed = monitoredSourceService.removeSource(sourceId);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(
                    chatId,
                    "✅ Джерело видалено:\n\nID: " + sourceId
            );
        } else {
            senderService.sendToChat(
                    chatId,
                    "⚠️ Джерело з таким ID не знайдено."
            );
        }
    }

    private void sendUnknownSources(String chatId) {

        var candidates = unknownSourceCandidateService.getAll();

        if (candidates.isEmpty()) {
            senderService.sendToChat(
                    chatId,
                    "Поки що немає знайдених невідомих джерел."
            );
            return;
        }

        StringBuilder builder = new StringBuilder();
        builder.append("🕵️ Знайдені невідомі джерела:\n\n");

        for (var candidate : candidates) {
            builder.append("📌 ")
                    .append(candidate.title())
                    .append("\n")
                    .append("ID: ")
                    .append(candidate.chatId())
                    .append("\n")
                    .append("Останній текст:\n")
                    .append(candidate.lastText())
                    .append("\n\n");
        }

        senderService.sendToChat(chatId, builder.toString());
    }
}