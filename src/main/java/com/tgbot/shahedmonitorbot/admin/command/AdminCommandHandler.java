package com.tgbot.shahedmonitorbot.admin.command;

import com.tgbot.shahedmonitorbot.admin.enums.*;
import com.tgbot.shahedmonitorbot.admin.menu.AdminMenuService;
import com.tgbot.shahedmonitorbot.admin.menu.DictionaryMenuService;
import com.tgbot.shahedmonitorbot.admin.service.*;
import com.tgbot.shahedmonitorbot.alertapi.formatter.ApiAlertStatusFormatter;
import com.tgbot.shahedmonitorbot.alertapi.service.AirAlertApiService;
import com.tgbot.shahedmonitorbot.manualalert.*;
import com.tgbot.shahedmonitorbot.model.admin.AdminSessionState;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;
import com.tgbot.shahedmonitorbot.monitoring.source.*;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import com.tgbot.shahedmonitorbot.tdlib.TdLibStatusService;

import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class AdminCommandHandler {

    private static final String SOURCE_ENABLE_CALLBACK = "source:enable:";
    private static final String SOURCE_IGNORE_CALLBACK = "source:ignore:";
    private static final ZoneId KYIV_ZONE = ZoneId.of("Europe/Kyiv");
    private static final DateTimeFormatter SOURCE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final TelegramSenderService senderService;
    private final AdminSessionService sessionService;
    private final AdminAccessService accessService;
    private final AdminMenuService menuService;
    private final ManualAlertService manualAlertService;
    private final AirAlertApiService airAlertApiService;
    private final ApiAlertStatusFormatter apiAlertStatusFormatter;
    private final MonitoredSourceService monitoredSourceService;
    private final UnknownSourceCandidateService unknownSourceCandidateService;
    private final MonitoringStateService monitoringStateService;
    private final TdLibStatusService tdLibStatusService;
    private final DictionaryAdminService dictionaryAdminService;
    private final DictionaryMenuService dictionaryMenuService;
    private final AdminMessageFormatter adminMessageFormatter;

    public AdminCommandHandler(
        TelegramSenderService senderService,
        AdminSessionService sessionService,
        AdminAccessService accessService,
        AdminMenuService menuService,
        ManualAlertService manualAlertService,
        AirAlertApiService airAlertApiService,
        ApiAlertStatusFormatter apiAlertStatusFormatter,
        MonitoredSourceService monitoredSourceService,
        UnknownSourceCandidateService unknownSourceCandidateService,
        MonitoringStateService monitoringStateService,
        TdLibStatusService tdLibStatusService,
        DictionaryAdminService dictionaryAdminService,
        DictionaryMenuService dictionaryMenuService,
        AdminMessageFormatter adminMessageFormatter
    ) {
        this.senderService = senderService;
        this.sessionService = sessionService;
        this.accessService = accessService;
        this.menuService = menuService;
        this.manualAlertService = manualAlertService;
        this.airAlertApiService = airAlertApiService;
        this.apiAlertStatusFormatter = apiAlertStatusFormatter;
        this.monitoredSourceService = monitoredSourceService;
        this.unknownSourceCandidateService = unknownSourceCandidateService;
        this.monitoringStateService = monitoringStateService;
        this.tdLibStatusService = tdLibStatusService;
        this.dictionaryAdminService = dictionaryAdminService;
        this.dictionaryMenuService = dictionaryMenuService;
        this.adminMessageFormatter = adminMessageFormatter;
    }

    public void handle(Long userId, String chatId, String text) {

        if (!accessService.isAdmin(userId)) {
            senderService.sendToChat(chatId, AdminMessage.NO_ACCESS.text());
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

        senderService.sendToChat(chatId, AdminMessage.UNKNOWN_COMMAND.text());
    }

    public void handleCallback(
        Long userId,
        String chatId,
        Integer messageId,
        String callbackQueryId,
        String callbackData
    ) {

        if (!accessService.isAdmin(userId)) {
            senderService.answerCallback(callbackQueryId, AdminMessage.NO_ACCESS.text());
            return;
        }

        if (callbackData == null || callbackData.isBlank()) {
            senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_ACTION.text());
            return;
        }

        if (callbackData.startsWith(SOURCE_ENABLE_CALLBACK)) {
            String sourceId = callbackData.substring(SOURCE_ENABLE_CALLBACK.length());
            enableSource(chatId, messageId, callbackQueryId, sourceId);
            return;
        }

        if (callbackData.startsWith(SOURCE_IGNORE_CALLBACK)) {
            String sourceId = callbackData.substring(SOURCE_IGNORE_CALLBACK.length());
            ignoreSource(chatId, messageId, callbackQueryId, sourceId);
            return;
        }

        if (callbackData.startsWith("dictionary:")) {

            handleDictionaryCallback(userId,
                chatId,
                messageId,
                callbackQueryId,
                callbackData
            );

            return;
        }

        senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_ACTION.text());
    }


    private void handleDictionaryCallback(
	    Long userId,
	    String chatId,
	    Integer messageId,
	    String callbackQueryId,
	    String callbackData
    ) {

	    String[] parts = callbackData.split(":", 4);

	    if (parts.length < 2) {
	    	senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_ACTION.text());
	    	return;
	    }

	    String action = parts[1];

	    switch (action) {

	    	case "category":

                if (parts.length < 4) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
                    return;
                }

                DictionaryType type = DictionaryType.valueOf(parts[2]);
                int categoryIndex;

                try {
                    categoryIndex = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
                    return;
                }

                List<String> categories = dictionaryAdminService.getCategories(type);

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.CATEGORY_NOT_FOUND.text());
                    return;
                }

                openDictionaryCategory(userId,
                    chatId,
                    messageId,
                    callbackQueryId,
                    type,
                    categories.get(categoryIndex)
                );
                return;

	    	case "categories":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_VOCAB.text());
	    			return;
	    		}

	    		showDictionaryCategories(chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2])
	    		);
	    		return;

	    	case "aliases":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
	    			return;
	    		}

	    		showDictionaryAliases(chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2]),
	    			parts[3]
	    		);
	    		return;

	    	case "add-alias":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
	    			return;
	    		}

	    		requestDictionaryAlias(userId,
	    			chatId,
	    			DictionaryType.valueOf(parts[2]),
	    			DictionaryAction.ADD,
	    			parts[3]
	    		);

	    		senderService.answerCallback(callbackQueryId, "");
	    		return;

	    	case "remove-alias":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
	    			return;
	    		}

	    		requestDictionaryAlias(userId,
	    			chatId,
	    			DictionaryType.valueOf(parts[2]),
	    			DictionaryAction.REMOVE,
	    			parts[3]
	    		);

	    		senderService.answerCallback(callbackQueryId, "");
	    		return;

	    	case "add-category":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_VOCAB.text());
	    			return;
	    		}

	    		requestNewCategory(userId, chatId, DictionaryType.valueOf(parts[2]));
	    		senderService.answerCallback(callbackQueryId, "");
	    		return;

	    	case "delete-mode":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_VOCAB.text());
	    			return;
	    		}

	    		showDeleteCategoryMenu(chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2])
	    		);
	    		return;

	    	case "delete-category":

                if (parts.length < 4) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
                    return;
                }

                DictionaryType type1 = DictionaryType.valueOf(parts[2]);
                int categoryIndex1;

                try {
                    categoryIndex1 = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.NO_CORRECT_CATEGORY.text());
                    return;
                }

                List<String> categories1 = dictionaryAdminService.getCategories(type1);

                if (categoryIndex1 < 0 || categoryIndex1 >= categories1.size()) {
                    senderService.answerCallback(callbackQueryId, AdminMessage.CATEGORY_NOT_FOUND.text());
                    return;
                }

                deleteDictionaryCategory(chatId,
                    messageId,
                    callbackQueryId,
                    type1,
                    categories1.get(categoryIndex1)
                );
                return;

	    	case "back":

	    		sendMainMenu(chatId);
	    		senderService.answerCallback(callbackQueryId, "");
	    		return;

	    	default:
	    		senderService.answerCallback(callbackQueryId, AdminMessage.UNKNOWN_ACTION.text());
	    }
    }

    private boolean handleSession(Long userId, String chatId, String text) {

	    AdminSessionState state = sessionService.getState(userId);

	    switch (state) {

	        case WAITING_FOR_DICTIONARY_INPUT:
            
	        	DictionaryType dictionaryType = sessionService.getDictionaryType(userId);
	        	DictionaryAction dictionaryAction = sessionService.getDictionaryAction(userId);
    
	        	if (dictionaryType == null || dictionaryAction == null) {
	        	    sessionService.reset(userId);
	        	    senderService.sendToChat(chatId, AdminMessage.UNKNOWN_ACTION_VOCAB.text());
	        	    return true;
	        	}
	        	if (text == null || text.isBlank()) {
	        	    senderService.sendToChat(chatId, AdminMessage.VALUE_CAN_NOT_BE_EMPTY.text());
	        	    return true;
	        	}
	        	handleDictionaryInput(userId, chatId, dictionaryType, dictionaryAction, text);

	        	return true;
    
            case WAITING_FOR_NEW_CATEGORY:

                DictionaryType type = sessionService.getDictionaryType(userId);
                boolean added = dictionaryAdminService.addCategory(type, text);
        
                sessionService.reset(userId);
                senderService.sendToChat(chatId,
                added ? "✅ Категорію «" + text + "» створено." : AdminMessage.CATEGORY_ALREADY_EXISTS.text());

	            return true;
    
	        case IDLE:
	        default:
	        	return false;
	    }
    }

    private boolean handleCommand(Long userId, String chatId, String text) {

        AdminCommand command = AdminCommand.fromText(text);

        if (command == null) {
            return false;
        }

        switch (command) {

            case START:
                senderService.sendToChat(chatId, AdminMessage.WELCOME_MESSAGE.text());
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
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.KEYWORDS_MENU.text(),
                    menuService.keywordsReplyKeyboard());
                return true;

            case TARGETS:
                sendDictionaryCategories(chatId, DictionaryType.TARGETS);
                return true;

            case LOCATIONS:
                sendDictionaryCategories(chatId, DictionaryType.LOCATIONS);
                return true;

            case DIRECTIONS:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.DIRECTIONS_MENU.text(),
                    menuService.dictionaryReplyKeyboard(DictionaryType.DIRECTIONS));
                return true;

            case SHOW_DIRECTIONS:
                sendDictionaryValues(chatId, DictionaryType.DIRECTIONS);
                return true;

            case ADD_DIRECTION:
                requestDictionaryInput(userId, chatId, DictionaryType.DIRECTIONS,
                    DictionaryAction.ADD, AdminMessage.ENTER_NEW_VALUE.text());
                return true;

            case REMOVE_DIRECTION:
                requestDictionaryInput(userId, chatId, DictionaryType.DIRECTIONS,
                    DictionaryAction.REMOVE, AdminMessage.REMOVE_VALUE.text());
                return true;

            case ATTENTION:
	            senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.ATTENTION_MENU.text(),
                    menuService.dictionaryReplyKeyboard(DictionaryType.ATTENTION));
	            return true;

            case SHOW_ATTENTIONS:
                sendDictionaryValues(chatId, DictionaryType.ATTENTION);
                return true;
        
            case ADD_ATTENTION:
                requestDictionaryInput(userId, chatId, DictionaryType.ATTENTION,
                    DictionaryAction.ADD, AdminMessage.ENTER_NEW_VALUE.text());
        	    return true;
        
            case REMOVE_ATTENTION:
                requestDictionaryInput(userId, chatId, DictionaryType.ATTENTION,
                    DictionaryAction.REMOVE, AdminMessage.REMOVE_VALUE.text());
                return true;
        
            case GLOBAL_THREAT:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.GLOBAL_THREATS_MENU.text(),
                    menuService.dictionaryReplyKeyboard(DictionaryType.GLOBAL_THREAT));
                return true;
        
            case SHOW_GLOBAL_THREATS:
                sendDictionaryValues(chatId, DictionaryType.GLOBAL_THREAT);
                return true;
        
            case ADD_GLOBAL_THREATS:
                requestDictionaryInput(userId, chatId, DictionaryType.GLOBAL_THREAT,
                    DictionaryAction.ADD, AdminMessage.ENTER_NEW_VALUE.text());
                return true;
        
            case REMOVE_GLOBAL_THREAT:
                requestDictionaryInput(userId, chatId, DictionaryType.GLOBAL_THREAT,
                    DictionaryAction.REMOVE, AdminMessage.REMOVE_VALUE.text());
                return true;
        
            case FORECAST:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.FORECAST_MENU.text(),
                    menuService.dictionaryReplyKeyboard(DictionaryType.FORECAST));
                return true;
        
            case SHOW_FORECASTS:
                sendDictionaryValues(chatId, DictionaryType.FORECAST);
                return true;
        
            case ADD_FORECAST:
                requestDictionaryInput(userId, chatId, DictionaryType.FORECAST,
                    DictionaryAction.ADD, AdminMessage.ENTER_NEW_VALUE.text());
                return true;
        
            case REMOVE_FORECAST:
                requestDictionaryInput(userId, chatId, DictionaryType.FORECAST,
                    DictionaryAction.REMOVE, AdminMessage.REMOVE_VALUE.text());
                return true;
        
            case NOISE:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.NOISE_MENU.text(),
                    menuService.dictionaryReplyKeyboard(DictionaryType.NOISE));
                return true;
        
            case SHOW_NOISES:
                sendDictionaryValues(chatId, DictionaryType.NOISE);
                return true;
        
            case ADD_NOISE:
                requestDictionaryInput(userId, chatId, DictionaryType.NOISE,
                    DictionaryAction.ADD, AdminMessage.ENTER_NEW_VALUE.text());
                return true;
        
            case REMOVE_NOISE:
                requestDictionaryInput(userId, chatId, DictionaryType.NOISE,
                    DictionaryAction.REMOVE, AdminMessage.REMOVE_VALUE.text());
                return true;

            case ALERTS:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.ALERT_MENU_TITLE.text(), menuService.alertReplyKeyboard());
                return true;

            case ALERT:
                sendManualAlert(chatId, AlertType.ALERT, AdminMessage.ALERT_SENT);
                return true;

            case HIGH_RISK:
                sendManualAlert(chatId, AlertType.HIGH_RISK, AdminMessage.HIGH_RISK_SENT);
                return true;

            case ALL_CLEAR:
                sendManualAlert(chatId, AlertType.ALL_CLEAR, AdminMessage.ALL_CLEAR_SENT);
                return true;

            case BACK:
                sendMainMenu(chatId);
                return true;

            case STATUS:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.STATUS_MENU.text(), menuService.statusReplyKeyboard());
                return true;

            case ALERT_STATUS:
                sendAlertStatus(chatId);
                return true;

            case BOT_STATUS:
                sendBotStatus(chatId);
                return true;

            case SOURCES:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.SOURCES_MENU.text(), menuService.sourcesReplyKeyboard());
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

    private void sendDictionaryCategories(String chatId, DictionaryType type) {
    
    	List<String> categories = dictionaryAdminService.getCategories(type);
    
    	if (categories.isEmpty()) {
    
    		senderService.sendToChatWithKeyboard(
    			chatId,
    			type == DictionaryType.TARGETS
    				? "🎯 " + AdminMessage.CATEGORIES_NOT_FOUND.text()
    				: "📍 " + AdminMessage.CATEGORIES_NOT_FOUND.text(),
    			dictionaryMenuService.categoriesKeyboard(
    				type,
    				categories,
    				false
    			)
    		);
    
    		return;
    	}
    
    	String title = type == DictionaryType.TARGETS
    		? AdminButton.TARGETS.text()
    		: AdminButton.LOCATIONS.text();

    	senderService.sendToChatWithKeyboard(
    		chatId,
    		title,
    		dictionaryMenuService.categoriesKeyboard(
    			type,
    			categories,
    			false
    		)
    	);
    }

    private void handleDictionaryInput(
        Long userId,
        String chatId,
        DictionaryType type,
        DictionaryAction action,
        String value
    ) {

        String category = sessionService.getSelectedCategory(userId);
        boolean categoryBased = type == DictionaryType.TARGETS || type == DictionaryType.LOCATIONS;

        // TARGETS і LOCATIONS працюють через категорії та аліаси.
        if (categoryBased && (category == null || category.isBlank())) {

            sessionService.reset(userId);
            senderService.sendToChat(chatId, "❌ Не вдалося визначити категорію.");
            return;
        }

        boolean success;

        if (categoryBased) {

            if (action == DictionaryAction.ADD) {
            success = dictionaryAdminService.addAlias(type, category, value);
            } else {
                success = dictionaryAdminService.removeAlias(type, category, value);
            }

        } else {

            // DIRECTIONS, ATTENTION, GLOBAL_THREAT, FORECAST, NOISE — прості списки.
            if (action == DictionaryAction.ADD) {
                success = dictionaryAdminService.add(type, value);
            } else {
                success = dictionaryAdminService.remove(type, value);
            }
        }

        sessionService.reset(userId);
        String message;

        if (action == DictionaryAction.ADD) {
            message = success ? "✅ Слово «" + value + "» додано." : "⚠️ Таке слово вже існує.";
        } else {
            message = success ? "✅ Слово «" + value + "» видалено." : "⚠️ Таке слово не знайдено.";
        }

        senderService.sendToChat(chatId, message);
    }

    private void sendDictionaryValues(String chatId, DictionaryType type) {
    
        List<String> values = dictionaryAdminService.getValues(type);
    
        if (values.isEmpty()) {
            senderService.sendToChat(chatId, "Список значень порожній.");
            return;
        }
    
        String title = switch (type) {
            case DIRECTIONS -> "🧭 Напрямки моніторингу";
            case ATTENTION -> "⚠️ Attention words";
            case GLOBAL_THREAT -> "🌐 Global threat markers";
            case FORECAST -> "🔮 Forecast markers";
            case NOISE -> "✂️ Noise markers";
            default -> "📋 Значення";
        };
        senderService.sendToChat(chatId, title + ":\n\n" + String.join("\n", values));
    }

    private void requestDictionaryInput(Long userId, String chatId, DictionaryType type, DictionaryAction action, String prompt) {
	    sessionService.setState(userId, AdminSessionState.WAITING_FOR_DICTIONARY_INPUT);
	    sessionService.setDictionaryType(userId, type);
	    sessionService.setDictionaryAction(userId, action);
	    senderService.sendToChat(chatId, prompt);
    }

    private void sendManualAlert(String chatId, AlertType type, AdminMessage successMessage) {
        manualAlertService.sendAlert(type);
        senderService.sendToChat(chatId, successMessage.text());
    }

    private void sendMainMenu(String chatId) {
        senderService.sendToChatWithReplyKeyboard(chatId, menuService.mainMenuText(), menuService.mainReplyKeyboard());
    }

    private String normalizeCommand(String text) {
        return text.replace("@bc_shahed_monitor_bot", "").trim();
    }

    private void sendBotStatus(String chatId) {

        String messageText = adminMessageFormatter.formatBotStatus(
            tdLibStatusService.isReady(),
            monitoredSourceService.getActiveSources().size(),
            monitoringStateService.isMonitoringEnabled(),
            monitoringStateService.getMonitoringActivationSource(),
            monitoringStateService.isAutoMode()
        );
        senderService.sendToChat(chatId, messageText);
    }

    private void sendAlertStatus(String chatId) {
        senderService.sendToChat(chatId, apiAlertStatusFormatter.format(airAlertApiService.getLastStatus()));
    }

    private void sendActiveSources(String chatId) {

        List<MonitoredSource> sources = monitoredSourceService.getActiveSources();

        if (sources.isEmpty()) {
            senderService.sendToChat(chatId, "📡 Активних джерел поки немає.");
            return;
        }

        senderService.sendToChat(chatId, adminMessageFormatter.formatActiveSourcesHeader(sources.size()));

        for (int index = 0; index < sources.size(); index++) {

            MonitoredSource source = sources.get(index);
            String message = adminMessageFormatter.formatSourceCard(source.title(), source.chatId(), "активне", index + 1, sources.size());

            InlineKeyboardMarkup keyboard = singleButtonKeyboard(
                "⛔ Вимкнути моніторинг",
                SOURCE_IGNORE_CALLBACK + source.chatId()
            );
            senderService.sendToChatWithKeyboard(chatId, message, keyboard);
        }
    }

    private void sendNewSources(String chatId) {

        List<UnknownSourceCandidate> candidates = unknownSourceCandidateService.getAll();

        if (candidates.isEmpty()) {
            senderService.sendToChat(chatId, "🆕 Нових джерел поки немає.");
            return;
        }

        senderService.sendToChat(chatId, adminMessageFormatter.formatNewSourcesHeader(candidates.size()));

        for (int index = 0; index < candidates.size(); index++) {

            UnknownSourceCandidate candidate = candidates.get(index);
            String message = adminMessageFormatter.formatNewSourceCandidateCard(candidate, index + 1, candidates.size());

            InlineKeyboardMarkup keyboard = twoButtonKeyboard(
                "✅ Увімкнути моніторинг",
                SOURCE_ENABLE_CALLBACK + candidate.chatId(),
                "⛔ Ігнорувати",
                SOURCE_IGNORE_CALLBACK + candidate.chatId()
            );
            senderService.sendToChatWithKeyboard(chatId, message, keyboard);
        }
    }

    private void sendIgnoredSources(String chatId) {

        List<MonitoredSource> sources = monitoredSourceService.getIgnoredSources();

        if (sources.isEmpty()) {
            senderService.sendToChat(chatId, "⛔ Ігнорованих джерел поки немає.");
            return;
        }

        senderService.sendToChat(chatId, adminMessageFormatter.formatIgnoredSourcesHeader(sources.size()));

        for (int index = 0; index < sources.size(); index++) {

            MonitoredSource source = sources.get(index);
            String message = adminMessageFormatter.formatSourceCard(source.title(), source.chatId(), "ігнороване", index + 1, sources.size());

            InlineKeyboardMarkup keyboard = singleButtonKeyboard(
                "✅ Увімкнути моніторинг",
                SOURCE_ENABLE_CALLBACK + source.chatId()
            );
            senderService.sendToChatWithKeyboard(chatId, message, keyboard);
        }
    }

    private void enableSource(String adminChatId, Integer messageId, String callbackQueryId, String sourceId) {

        UnknownSourceCandidate candidate = unknownSourceCandidateService.findByChatId(sourceId);

        // NEW → ACTIVE
        if (candidate != null) {

            boolean added = monitoredSourceService.addActiveSource(candidate.chatId(), candidate.title());

            if (added) {

                unknownSourceCandidateService.remove(candidate.chatId());
                senderService.answerCallback(callbackQueryId, "Моніторинг увімкнено");

                String message = adminMessageFormatter.formatSourceCard(candidate.title(), candidate.chatId(), "активне", null, null);

                senderService.editMessageWithKeyboard(
                    adminChatId,
                    messageId,
                    message,
                    singleButtonKeyboard(
                        "⛔ Вимкнути моніторинг",
                        SOURCE_IGNORE_CALLBACK + candidate.chatId()
                    )
                );
                return;
            }
        }

        // IGNORED → ACTIVE
        MonitoredSource source = monitoredSourceService.findByChatId(sourceId);

        if (source == null) {
            senderService.answerCallback(callbackQueryId, "Джерело не знайдено");
            return;
        }

        boolean enabled = monitoredSourceService.enableSource(sourceId);

        if (enabled) {

            senderService.answerCallback(callbackQueryId, "Моніторинг увімкнено");
            String message = adminMessageFormatter.formatSourceCard(source.title(), source.chatId(), "активне", null, null);

            senderService.editMessageWithKeyboard(
                adminChatId,
                messageId,
                message,
                singleButtonKeyboard(
                    "⛔ Вимкнути моніторинг",
                    SOURCE_IGNORE_CALLBACK + source.chatId()
                )
            );
            return;
        }
        senderService.answerCallback(callbackQueryId, "Стан уже актуальний");
    }

    private void ignoreSource(String adminChatId, Integer messageId, String callbackQueryId, String sourceId) {

        UnknownSourceCandidate candidate = unknownSourceCandidateService.findByChatId(sourceId);

        // NEW → IGNORED
        if (candidate != null) {

            boolean added = monitoredSourceService.addIgnoredSource(candidate.chatId(), candidate.title());

            if (added) {

                unknownSourceCandidateService.remove(candidate.chatId());
                senderService.answerCallback(callbackQueryId, "Джерело ігнорується");

                String message = adminMessageFormatter.formatSourceCard(candidate.title(), candidate.chatId(), "ігнороване", null, null);

                senderService.editMessageWithKeyboard(
                    adminChatId,
                    messageId,
                    message,
                    singleButtonKeyboard(
                        "✅ Увімкнути моніторинг",
                        SOURCE_ENABLE_CALLBACK + candidate.chatId()
                    )
                );
                return;
            }
        }

        // ACTIVE → IGNORED
        MonitoredSource source = monitoredSourceService.findByChatId(sourceId);

        if (source == null) {
            senderService.answerCallback(callbackQueryId, "Джерело не знайдено");
            return;
        }

        boolean ignored = monitoredSourceService.ignoreSource(sourceId);

        if (ignored) {

            senderService.answerCallback(callbackQueryId, "Моніторинг вимкнено");

            String message = adminMessageFormatter.formatSourceCard(source.title(), source.chatId(), "ігнороване", null, null);

            senderService.editMessageWithKeyboard(
                adminChatId,
                messageId,
                message,
                singleButtonKeyboard("✅ Увімкнути моніторинг", SOURCE_ENABLE_CALLBACK + source.chatId())
            );

            return;
        }

        senderService.answerCallback(callbackQueryId, "Стан уже актуальний");
    }

    private InlineKeyboardMarkup singleButtonKeyboard(String text, String callbackData) {

        InlineKeyboardButton button = InlineKeyboardButton.builder().text(text).callbackData(callbackData).build();
        InlineKeyboardRow row = new InlineKeyboardRow();

        row.add(button);
        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    private InlineKeyboardMarkup twoButtonKeyboard(String firstText, String firstCallback, String secondText, String secondCallback) {

        InlineKeyboardButton firstButton = InlineKeyboardButton.builder().text(firstText).callbackData(firstCallback).build();
        InlineKeyboardButton secondButton = InlineKeyboardButton.builder().text(secondText).callbackData(secondCallback).build();

        InlineKeyboardRow row = new InlineKeyboardRow();

        row.add(firstButton);
        row.add(secondButton);

        return InlineKeyboardMarkup.builder().keyboard(List.of(row)).build();
    }

    private void openDictionaryCategory(
        Long userId,
        String chatId,
        Integer messageId,
        String callbackQueryId,
        DictionaryType type,
        String category
    ) {

        sessionService.setDictionaryType(userId, type);
        sessionService.setSelectedCategory(userId, category);

        List<String> aliases = dictionaryAdminService.getAliases(type, category);
        String icon = type == DictionaryType.TARGETS ? "🎯 " : "📍 ";
        StringBuilder messageText = new StringBuilder();
        messageText.append(icon).append("Категорія: *").append(category).append("*\n\n");

        if (aliases.isEmpty()) {
            messageText.append("📋 Аліасів поки немає.");
        } else {
            messageText.append("📋 Аліаси:\n");
            for (String alias : aliases) {
                messageText.append("• ").append(alias).append("\n");
            }
        }

        senderService.editMessageWithKeyboard(
            chatId,
            messageId,
            messageText.toString(),
            dictionaryMenuService.categoryKeyboard(type, category)
        );

        senderService.answerCallback(callbackQueryId, "");
    }

    private void showDictionaryCategories(String chatId, Integer messageId, String callbackQueryId, DictionaryType type) {

        List<String> categories = dictionaryAdminService.getCategories(type);
        String title = type == DictionaryType.TARGETS ? "🎯 Цілі" : "📍 Локації";

        senderService.editMessageWithKeyboard(
            chatId,
            messageId,
            title,
            dictionaryMenuService.categoriesKeyboard(type, categories, false)
        );

        senderService.answerCallback(callbackQueryId, "");
    }

    private void showDictionaryAliases(
        String chatId,
        Integer messageId,
        String callbackQueryId,
        DictionaryType type,
        String category
    ) {

        List<String> aliases = dictionaryAdminService.getAliases(type, category);
        String message;

        if (aliases.isEmpty()) {
            message = "📋 Аліасів поки немає.";
        } else {
            message = "📋 Аліаси:\n\n" + String.join("\n", aliases);
        }

        senderService.editMessageWithKeyboard(
            chatId,
            messageId,
            message,
            dictionaryMenuService.categoryKeyboard(type, category)
        );

        senderService.answerCallback(callbackQueryId, "");
    }

    private void requestDictionaryAlias(
        Long userId,
        String chatId,
        DictionaryType type,
        DictionaryAction action,
        String category
    ) {

        sessionService.setState(userId, AdminSessionState.WAITING_FOR_DICTIONARY_INPUT);

        sessionService.setDictionaryType(userId, type);
        sessionService.setDictionaryAction(userId, action);
        sessionService.setSelectedCategory(userId, category);

        String message = action == DictionaryAction.ADD
            ? "Надішліть новий аліас для категорії «" + category + "»."
            : "Надішліть аліас, який потрібно видалити з категорії «" + category + "».";

        senderService.sendToChat(chatId, message);
    }

    private void requestNewCategory(Long userId, String chatId, DictionaryType type) {
        sessionService.setState(userId, AdminSessionState.WAITING_FOR_NEW_CATEGORY);
        sessionService.setDictionaryType(userId, type);
        senderService.sendToChat(chatId, "Введіть назву нової категорії:");
    }

    private void showDeleteCategoryMenu(String chatId, Integer messageId, String callbackQueryId, DictionaryType type) {

        List<String> categories = dictionaryAdminService.getCategories(type);

        senderService.editMessageWithKeyboard(
            chatId,
            messageId,
            "➖ Оберіть категорію, яку потрібно видалити:",
            dictionaryMenuService.deleteCategoriesKeyboard(type, categories)
        );

        senderService.answerCallback(callbackQueryId, "");
    }

    private void deleteDictionaryCategory(
        String chatId,
        Integer messageId,
        String callbackQueryId,
        DictionaryType type,
        String category
    ) {

        boolean removed = dictionaryAdminService.removeCategory(type, category);

        if (!removed) {
            senderService.answerCallback(callbackQueryId, "Категорію не знайдено");
            return;
        }

        senderService.answerCallback(callbackQueryId, "Категорію видалено");
        showDictionaryCategories(chatId, messageId, callbackQueryId, type);
    }
}