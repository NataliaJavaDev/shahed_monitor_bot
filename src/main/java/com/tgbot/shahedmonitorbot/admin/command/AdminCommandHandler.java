package com.tgbot.shahedmonitorbot.admin.command;

import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.alertapi.formatter.ApiAlertStatusFormatter;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidateService;
import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;
import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.admin.menu.AdminMenuService;
import com.tgbot.shahedmonitorbot.admin.service.AdminAccessService;
import com.tgbot.shahedmonitorbot.admin.service.AdminSessionService;
import com.tgbot.shahedmonitorbot.admin.service.DirectionAdminService;
import com.tgbot.shahedmonitorbot.admin.service.LocationAdminService;
import com.tgbot.shahedmonitorbot.admin.service.TargetAdminService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertService;
import com.tgbot.shahedmonitorbot.manualalert.ManualAlertType;
import com.tgbot.shahedmonitorbot.admin.enums.*;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidate;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.*;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;


@Service
public class AdminCommandHandler {

    private static final String SOURCE_ENABLE_CALLBACK =
            "source:enable:";

    private static final String SOURCE_IGNORE_CALLBACK =
            "source:ignore:";

    private static final ZoneId KYIV_ZONE =
            ZoneId.of("Europe/Kyiv");

    private static final DateTimeFormatter SOURCE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TargetAdminService targetAdminService;
    private final LocationAdminService locationAdminService;
    private final DirectionAdminService directionAdminService;
    private final TelegramSenderService senderService;
    private final AdminSessionService sessionService;
    private final AdminAccessService accessService;
    private final AdminMenuService menuService;
    private final ManualAlertService manualAlertService;
    private final AirAlertApiService airAlertApiService;
    private final ApiAlertStatusFormatter apiAlertStatusFormatter;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;

    public AdminCommandHandler(
            TargetAdminService targetAdminService,
            LocationAdminService locationAdminService,
            DirectionAdminService directionAdminService,
            TelegramSenderService senderService,
            AdminSessionService sessionService,
            AdminAccessService accessService,
            AdminMenuService menuService,
            ManualAlertService manualAlertService,
            AirAlertApiService airAlertApiService,
            ApiAlertStatusFormatter apiAlertStatusFormatter,
            MonitoredSourceService monitoredSourceService,
            UnknownSourceCandidateService unknownSourceCandidateService
    ) {
        this.targetAdminService = targetAdminService;
        this.locationAdminService = locationAdminService;
        this.directionAdminService = directionAdminService;
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
        this.manualAlertService = manualAlertService;
        this.airAlertApiService = airAlertApiService;
        this.apiAlertStatusFormatter = apiAlertStatusFormatter;
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

    public void handleCallback(
            Long userId,
            String chatId,
            String callbackQueryId,
            String callbackData
    ) {

        if (!accessService.isAdmin(userId)) {
            senderService.answerCallback(
                    callbackQueryId,
                    "Немає доступу"
            );
            return;
        }

        if (callbackData == null
                || callbackData.isBlank()) {
            senderService.answerCallback(
                    callbackQueryId,
                    "Некоректна дія"
            );
            return;
        }

        if (callbackData.startsWith(
                SOURCE_ENABLE_CALLBACK
        )) {
            String sourceId = callbackData.substring(
                    SOURCE_ENABLE_CALLBACK.length()
            );

            enableSource(
                    chatId,
                    callbackQueryId,
                    sourceId
            );

            return;
        }

        if (callbackData.startsWith(
                SOURCE_IGNORE_CALLBACK
        )) {
            String sourceId = callbackData.substring(
                    SOURCE_IGNORE_CALLBACK.length()
            );

            ignoreSource(
                    chatId,
                    callbackQueryId,
                    sourceId
            );

            return;
        }

        senderService.answerCallback(
                callbackQueryId,
                "Невідома дія"
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

        if (state == AdminSessionState.WAITING_FOR_NEW_DIRECTION) {
            addDirection(userId, chatId, text);
            return true;
        }

        if (state == AdminSessionState.WAITING_FOR_REMOVE_DIRECTION) {
            removeDirection(userId, chatId, text);
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

            case DIRECTIONS:
                senderService.sendToChatWithReplyKeyboard(
                        chatId,
                        "🧭 Напрямки\n\nОберіть дію:",
                        menuService.directionsReplyKeyboard()
                );
                return true;

            case SHOW_DIRECTIONS:
                sendDirections(chatId);
                return true;

            case ADD_DIRECTION:
                requestAddDirection(userId, chatId);
                return true;

            case REMOVE_DIRECTION:
                requestRemoveDirection(userId, chatId);
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

            case SOURCES:
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                "📡 Джерела моніторингу\n\nОберіть дію:",
                menuService.sourcesReplyKeyboard()
                );
                return true;

            case ACTIVE_SOURCES:
                sendActiveSources(chatId);
                return true;

            case NEW_SOURCES:
                sendNewSources(chatId);
                return true;

            case IGNORED_SOURCES:
                sendIgnoredSources(chatId);
                return true;
        }
        return false;
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

    private void sendDirections(String chatId) {
        String directions = String.join(
                "\n",
                directionAdminService.getDirections()
        );

        if (directions.isBlank()) {
            senderService.sendToChat(chatId, "Список напрямків порожній.");
            return;
        }

        senderService.sendToChat(
                chatId,
                "🧭 Напрямки моніторингу:\n\n" + directions
        );
    }

    private void addDirection(Long userId, String chatId, String direction) {
        boolean added = directionAdminService.addDirection(direction);

        sessionService.reset(userId);

        if (added) {
            senderService.sendToChat(chatId, "✅ Напрямок додано: " + direction);
        } else {
            senderService.sendToChat(chatId, "⚠️ Такий напрямок уже додано або значення порожнє.");
        }
    }

    private void removeDirection(Long userId, String chatId, String direction) {
        boolean removed = directionAdminService.removeDirection(direction);

        sessionService.reset(userId);

        if (removed) {
            senderService.sendToChat(chatId, "✅ Напрямок видалено: " + direction);
        } else {
            senderService.sendToChat(chatId, "⚠️ Такий напрямок не знайдено.");
        }
    }

    private void requestAddDirection(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_NEW_DIRECTION);
        senderService.sendToChat(chatId, "Введіть напрямок для моніторингу:");
    }

    private void requestRemoveDirection(Long userId, String chatId) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_REMOVE_DIRECTION);
        senderService.sendToChat(chatId, "Введіть напрямок, який треба видалити:");
    }

    private String normalizeCommand(String text) {

        return text.replace("@bc_shahed_monitor_bot", "")
                .trim();
    }

    private void sendBotStatus(String chatId) {

        String message = """
                🤖 Статус бота
                
                TDLib-моніторинг каналів: увімкнений
                API-керування моніторингом: увімкнене
            """;

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

    private void sendActiveSources(String chatId) {

        List<MonitoredSource> sources =
                monitoredSourceService.getActiveSources();

        if (sources.isEmpty()) {
            senderService.sendToChat(
                    chatId,
                    "✅ Активних джерел поки немає."
            );
            return;
        }

        senderService.sendToChat(
                chatId,
                "✅ Активні джерела: "
                        + sources.size()
        );

        for (MonitoredSource source : sources) {

            String message = """
                    📡 %s

                    ID: %s
                    Статус: ✅ моніторинг увімкнено
                    """.formatted(
                    source.title(),
                    source.chatId()
            );

            InlineKeyboardMarkup keyboard =
                    singleButtonKeyboard(
                            "⛔ Ігнорувати",
                            SOURCE_IGNORE_CALLBACK
                                    + source.chatId()
                    );

            senderService.sendToChatWithKeyboard(
                    chatId,
                    message,
                    keyboard
            );
        }
    }

    private void sendNewSources(String chatId) {

        List<UnknownSourceCandidate> candidates =
                unknownSourceCandidateService.getAll();

        if (candidates.isEmpty()) {
            senderService.sendToChat(
                    chatId,
                    "🆕 Нових джерел поки немає."
            );
            return;
        }

        senderService.sendToChat(
                chatId,
                "🆕 Нові джерела: "
                        + candidates.size()
        );

        for (UnknownSourceCandidate candidate : candidates) {

            String message = """
                    📡 %s

                    ID: %s
                    Вперше знайдено: %s
                    Остання активність: %s

                    Останнє повідомлення:
                    %s
                    """.formatted(
                    candidate.title(),
                    candidate.chatId(),
                    formatInstant(candidate.firstSeenAt()),
                    formatInstant(candidate.lastSeenAt()),
                    candidate.lastText()
            );

            InlineKeyboardMarkup keyboard =
                    twoButtonKeyboard(
                            "👁 Увімкнути",
                            SOURCE_ENABLE_CALLBACK
                                    + candidate.chatId(),
                            "⛔ Ігнорувати",
                            SOURCE_IGNORE_CALLBACK
                                    + candidate.chatId()
                    );

            senderService.sendToChatWithKeyboard(
                    chatId,
                    message,
                    keyboard
            );
        }
    }

    private void sendIgnoredSources(String chatId) {

        List<MonitoredSource> sources =
                monitoredSourceService.getIgnoredSources();

        if (sources.isEmpty()) {
            senderService.sendToChat(
                    chatId,
                    "⛔ Ігнорованих джерел поки немає."
            );
            return;
        }

        senderService.sendToChat(
                chatId,
                "⛔ Ігноровані джерела: "
                        + sources.size()
        );

        for (MonitoredSource source : sources) {

            String message = """
                    📡 %s

                    ID: %s
                    Статус: ⛔ джерело ігнорується
                    """.formatted(
                    source.title(),
                    source.chatId()
            );

            InlineKeyboardMarkup keyboard =
                    singleButtonKeyboard(
                            "👁 Увімкнути",
                            SOURCE_ENABLE_CALLBACK
                                    + source.chatId()
                    );

            senderService.sendToChatWithKeyboard(
                    chatId,
                    message,
                    keyboard
            );
        }
    }

    private void enableSource(
            String adminChatId,
            String callbackQueryId,
            String sourceId
    ) {

        UnknownSourceCandidate candidate =
                unknownSourceCandidateService
                        .findByChatId(sourceId);

        if (candidate != null) {

            boolean added =
                    monitoredSourceService.addActiveSource(
                            candidate.chatId(),
                            candidate.title()
                    );

            if (added) {
                unknownSourceCandidateService.remove(
                        candidate.chatId()
                );

                senderService.answerCallback(
                        callbackQueryId,
                        "Джерело увімкнено"
                );

                senderService.sendToChat(
                        adminChatId,
                        "✅ Джерело додано до моніторингу:\n\n"
                                + candidate.title()
                                + "\nID: "
                                + candidate.chatId()
                );

                return;
            }
        }

        boolean enabled =
                monitoredSourceService.enableSource(sourceId);

        if (enabled) {
            senderService.answerCallback(
                    callbackQueryId,
                    "Джерело увімкнено"
            );

            MonitoredSource source =
                    monitoredSourceService
                            .findByChatId(sourceId);

            senderService.sendToChat(
                    adminChatId,
                    "✅ Моніторинг джерела увімкнено:\n\n"
                            + source.title()
                            + "\nID: "
                            + source.chatId()
            );

            return;
        }

        senderService.answerCallback(
                callbackQueryId,
                "Стан не змінено"
        );
    }

    private void ignoreSource(
            String adminChatId,
            String callbackQueryId,
            String sourceId
    ) {

        UnknownSourceCandidate candidate =
                unknownSourceCandidateService
                        .findByChatId(sourceId);

        if (candidate != null) {

            boolean added =
                    monitoredSourceService.addIgnoredSource(
                            candidate.chatId(),
                            candidate.title()
                    );

            if (added) {
                unknownSourceCandidateService.remove(
                        candidate.chatId()
                );

                senderService.answerCallback(
                        callbackQueryId,
                        "Джерело ігнорується"
                );

                senderService.sendToChat(
                        adminChatId,
                        "⛔ Джерело додано до ігнорованих:\n\n"
                                + candidate.title()
                                + "\nID: "
                                + candidate.chatId()
                );

                return;
            }
        }

        boolean ignored =
                monitoredSourceService.ignoreSource(sourceId);

        if (ignored) {
            senderService.answerCallback(
                    callbackQueryId,
                    "Джерело вимкнено"
            );

            MonitoredSource source =
                    monitoredSourceService
                            .findByChatId(sourceId);

            senderService.sendToChat(
                    adminChatId,
                    "⛔ Джерело більше не моніториться:\n\n"
                            + source.title()
                            + "\nID: "
                            + source.chatId()
            );

            return;
        }

        senderService.answerCallback(
                callbackQueryId,
                "Стан не змінено"
        );
    }

    private InlineKeyboardMarkup singleButtonKeyboard(
            String text,
            String callbackData
    ) {

        InlineKeyboardButton button =
                InlineKeyboardButton.builder()
                        .text(text)
                        .callbackData(callbackData)
                        .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(button);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }

    private InlineKeyboardMarkup twoButtonKeyboard(
            String firstText,
            String firstCallback,
            String secondText,
            String secondCallback
    ) {

        InlineKeyboardButton firstButton =
                InlineKeyboardButton.builder()
                        .text(firstText)
                        .callbackData(firstCallback)
                        .build();

        InlineKeyboardButton secondButton =
                InlineKeyboardButton.builder()
                        .text(secondText)
                        .callbackData(secondCallback)
                        .build();

        InlineKeyboardRow row = new InlineKeyboardRow();
        row.add(firstButton);
        row.add(secondButton);

        return InlineKeyboardMarkup.builder()
                .keyboard(List.of(row))
                .build();
    }

    private String formatInstant(java.time.Instant instant) {

        if (instant == null) {
            return "невідомо";
        }

        return SOURCE_TIME_FORMATTER.format(
                instant.atZone(KYIV_ZONE)
        );
    }
}