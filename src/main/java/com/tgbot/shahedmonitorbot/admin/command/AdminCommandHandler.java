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
import java.util.ArrayList;
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
        DictionaryMenuService dictionaryMenuService
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
            senderService.answerCallback(callbackQueryId, "Немає доступу");
            return;
        }

        if (callbackData == null || callbackData.isBlank()) {

            senderService.answerCallback(callbackQueryId, "Некоректна дія");
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

	    handleDictionaryCallback(
		userId,
		chatId,
		messageId,
		callbackQueryId,
		callbackData
	    );

	    return;
        }

        senderService.answerCallback(callbackQueryId, "Невідома дія");
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
	    	senderService.answerCallback(
	    		callbackQueryId,
	    		"Некоректна дія"
	    	);
	    	return;
	    }

	    String action = parts[1];

	    switch (action) {

	    	case "category":

                if (parts.length < 4) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Некоректна категорія"
                    );
                    return;
                }

                DictionaryType type = DictionaryType.valueOf(parts[2]);
                int categoryIndex;

                try {
                    categoryIndex = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Некоректна категорія"
                    );
                    return;
                }

                List<String> categories = dictionaryAdminService.getCategories(type);

                if (categoryIndex < 0 || categoryIndex >= categories.size()) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Категорію не знайдено"
                    );
                    return;
                }

                openDictionaryCategory(
                    userId,
                    chatId,
                    messageId,
                    callbackQueryId,
                    type,
                    categories.get(categoryIndex)
                );
                return;

	    	case "categories":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректний словник"
	    			);
	    			return;
	    		}

	    		showDictionaryCategories(
	    			chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2])
	    		);
	    		return;

	    	case "aliases":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректна категорія"
	    			);
	    			return;
	    		}

	    		showDictionaryAliases(
	    			chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2]),
	    			parts[3]
	    		);
	    		return;

	    	case "add-alias":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректна категорія"
	    			);
	    			return;
	    		}

	    		requestDictionaryAlias(
	    			userId,
	    			chatId,
	    			DictionaryType.valueOf(parts[2]),
	    			DictionaryAction.ADD,
	    			parts[3]
	    		);

	    		senderService.answerCallback(
	    			callbackQueryId,
	    			""
	    		);
	    		return;

	    	case "remove-alias":

	    		if (parts.length < 4) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректна категорія"
	    			);
	    			return;
	    		}

	    		requestDictionaryAlias(
	    			userId,
	    			chatId,
	    			DictionaryType.valueOf(parts[2]),
	    			DictionaryAction.REMOVE,
	    			parts[3]
	    		);

	    		senderService.answerCallback(
	    			callbackQueryId,
	    			""
	    		);
	    		return;

	    	case "add-category":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректний словник"
	    			);
	    			return;
	    		}

	    		requestNewCategory(
	    			userId,
	    			chatId,
	    			DictionaryType.valueOf(parts[2])
	    		);

	    		senderService.answerCallback(
	    			callbackQueryId,
	    			""
	    		);
	    		return;

	    	case "delete-mode":

	    		if (parts.length < 3) {
	    			senderService.answerCallback(
	    				callbackQueryId,
	    				"Некоректний словник"
	    			);
	    			return;
	    		}

	    		showDeleteCategoryMenu(
	    			chatId,
	    			messageId,
	    			callbackQueryId,
	    			DictionaryType.valueOf(parts[2])
	    		);
	    		return;

	    	case "delete-category":

                if (parts.length < 4) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Некоректна категорія"
                    );
                    return;
                }

                DictionaryType type1 = DictionaryType.valueOf(parts[2]);

                int categoryIndex1;

                try {
                    categoryIndex1 = Integer.parseInt(parts[3]);
                } catch (NumberFormatException e) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Некоректна категорія"
                    );
                    return;
                }

                List<String> categories1 = dictionaryAdminService.getCategories(type1);

                if (categoryIndex1 < 0 || categoryIndex1 >= categories1.size()) {
                    senderService.answerCallback(
                        callbackQueryId,
                        "Категорію не знайдено"
                    );
                    return;
                }

                deleteDictionaryCategory(
                    chatId,
                    messageId,
                    callbackQueryId,
                    type1,
                    categories1.get(categoryIndex1)
                );
                return;

	    	case "back":

	    		sendMainMenu(chatId);

	    		senderService.answerCallback(
	    			callbackQueryId,
	    			""
	    		);
	    		return;

	    	default:

	    		senderService.answerCallback(
	    			callbackQueryId,
	    			"Невідома дія"
	    		);
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
	        	    senderService.sendToChat(chatId, "❌ Не вдалося визначити операцію зі словником. Спробуйте ще раз.");
	        	    return true;
	        	}
	        	if (text == null || text.isBlank()) {
	        	    senderService.sendToChat(chatId, "⚠️ Значення не може бути порожнім.");
	        	    return true;
	        	}
	        	handleDictionaryInput(userId, chatId, dictionaryType, dictionaryAction, text);
	        	return true;
    
                case WAITING_FOR_NEW_CATEGORY:
                
	            DictionaryType type = sessionService.getDictionaryType(userId);
	            boolean added = dictionaryAdminService.addCategory(type, text);
    
	            sessionService.reset(userId);
    
	            senderService.sendToChat(
	    	    chatId,
	    	    added ? "✅ Категорію «" + text + "» створено." : "⚠️ Така категорія вже існує.");
    
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
                senderService.sendToChatWithReplyKeyboard(
                    chatId,
                    "🔑 Ключові слова\n\nОберіть розділ:",
                    menuService.keywordsReplyKeyboard()
                );
                return true;

            case TARGETS:
                sendDictionaryCategories(chatId, DictionaryType.TARGETS);
                return true;

            case LOCATIONS:
                sendDictionaryCategories(chatId, DictionaryType.LOCATIONS);
                return true;

            case SHOW_LOCATIONS:
                sendDictionaryValues(chatId, DictionaryType.LOCATIONS);
                return true;

            case ADD_LOCATION:
                requestDictionaryInput(userId, chatId, DictionaryType.LOCATIONS, DictionaryAction.ADD, "Введіть населений пункт для моніторингу:");
                return true;

            case REMOVE_LOCATION:
                requestDictionaryInput(userId, chatId, DictionaryType.LOCATIONS, DictionaryAction.REMOVE, "Введіть населений пункт, який треба видалити:");
                return true;

            case DIRECTIONS:
                senderService.sendToChatWithReplyKeyboard(chatId,
                    "🧭 Напрямки\n\nОберіть дію:",
                    menuService.dictionaryReplyKeyboard(DictionaryType.DIRECTIONS)
                );
                return true;

            case SHOW_DIRECTIONS:
                sendDictionaryValues(chatId, DictionaryType.DIRECTIONS);
                return true;

            case ADD_DIRECTION:
                requestDictionaryInput(userId, chatId, DictionaryType.DIRECTIONS, DictionaryAction.ADD, "Введіть напрямок для моніторингу:");
                return true;

            case REMOVE_DIRECTION:
                requestDictionaryInput(userId, chatId, DictionaryType.DIRECTIONS, DictionaryAction.REMOVE, "Введіть напрямок, який треба видалити:");
                return true;

            case ATTENTION:
	            senderService.sendToChatWithReplyKeyboard(chatId,
		        "⚠️ Attention words\n\nОберіть дію:",
		        menuService.dictionaryReplyKeyboard(DictionaryType.ATTENTION)
	);
	return true;

            case SHOW_ATTENTIONS:
        	sendDictionaryValues(chatId, DictionaryType.ATTENTION);
        	return true;
        
            case ADD_ATTENTION:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.ATTENTION,
        		DictionaryAction.ADD,
        		"Введіть новий attention marker:"
        	);
        	return true;
        
            case REMOVE_ATTENTION:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.ATTENTION,
        		DictionaryAction.REMOVE,
        		"Введіть attention marker, який треба видалити:"
        	);
        	return true;
        
            case GLOBAL_THREAT:
        	senderService.sendToChatWithReplyKeyboard(
        		chatId,
        		"🌐 Global threat markers\n\nОберіть дію:",
        		menuService.dictionaryReplyKeyboard(DictionaryType.GLOBAL_THREAT)
        	);
        	return true;
        
            case SHOW_GLOBAL_THREATS:
        	sendDictionaryValues(chatId, DictionaryType.GLOBAL_THREAT);
        	return true;
        
            case ADD_GLOBAL_THREATS:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.GLOBAL_THREAT,
        		DictionaryAction.ADD,
        		"Введіть новий global threat marker:"
        	);
        	return true;
        
            case REMOVE_GLOBAL_THREAT:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.GLOBAL_THREAT,
        		DictionaryAction.REMOVE,
        		"Введіть global threat marker, який треба видалити:"
        	);
        	return true;
        
            case FORECAST:
        	senderService.sendToChatWithReplyKeyboard(
        		chatId,
        		"🔮 Forecast markers\n\nОберіть дію:",
        		menuService.dictionaryReplyKeyboard(DictionaryType.FORECAST)
        	);
        	return true;
        
            case SHOW_FORECASTS:
        	sendDictionaryValues(chatId, DictionaryType.FORECAST);
        	return true;
        
            case ADD_FORECAST:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.FORECAST,
        		DictionaryAction.ADD,
        		"Введіть новий forecast marker:"
        	);
        	return true;
        
            case REMOVE_FORECAST:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.FORECAST,
        		DictionaryAction.REMOVE,
        		"Введіть forecast marker, який треба видалити:"
        	);
        	return true;
        
            case NOISE:
        	senderService.sendToChatWithReplyKeyboard(
        		chatId,
        		"✂️ Noise markers\n\nОберіть дію:",
        		menuService.dictionaryReplyKeyboard(DictionaryType.NOISE)
        	);
        	return true;
        
            case SHOW_NOISES:
        	sendDictionaryValues(chatId, DictionaryType.NOISE);
        	return true;
        
            case ADD_NOISE:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.NOISE,
        		DictionaryAction.ADD,
        		"Введіть новий noise marker:"
        	);
        	return true;
        
            case REMOVE_NOISE:
        	requestDictionaryInput(
        		userId,
        		chatId,
        		DictionaryType.NOISE,
        		DictionaryAction.REMOVE,
        		"Введіть noise marker, який треба видалити:"
        	);
        	return true;

            case ALERTS:
                senderService.sendToChatWithReplyKeyboard(chatId, AdminMessage.ALERT_MENU_TITLE.text(), menuService.alertReplyKeyboard());
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
                senderService.sendToChatWithReplyKeyboard(chatId, "📊 Статус\n\nОберіть дію:", menuService.statusReplyKeyboard());
                return true;

            case ALERT_STATUS:
                sendAlertStatus(chatId);
                return true;

            case BOT_STATUS:
                sendBotStatus(chatId);
                return true;

            case SOURCES:
                senderService.sendToChatWithReplyKeyboard(chatId, "📡 Джерела моніторингу\n\nОберіть дію:", menuService.sourcesReplyKeyboard());
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
    				? "🎯 Категорій цілей поки немає."
    				: "📍 Категорій локацій поки немає.",
    			dictionaryMenuService.categoriesKeyboard(
    				type,
    				categories,
    				false
    			)
    		);
    
    		return;
    	}
    
    	String title = type == DictionaryType.TARGETS
    		? "🎯 Цілі"
    		: "📍 Локації";
    
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

	/*
	 * TARGETS і LOCATIONS працюють через категорії та аліаси.
	 */
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

	    /*
	     * DIRECTIONS, ATTENTION, GLOBAL_THREAT,
	     * FORECAST, NOISE — прості списки.
	     */
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
            senderService.sendToChat(
                chatId,
                "Список значень порожній."
            );
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
    
        senderService.sendToChat(
            chatId,
            title + ":\n\n" + String.join("\n", values)
        );
    }

    private void requestDictionaryInput(Long userId, String chatId, DictionaryType type, DictionaryAction action, String prompt) {

	    sessionService.setState(userId, AdminSessionState.WAITING_FOR_DICTIONARY_INPUT);
	    sessionService.setDictionaryType(userId, type);
	    sessionService.setDictionaryAction(userId, action);
	    senderService.sendToChat(chatId, prompt);
    }

    private void sendManualAlert(String chatId, ManualAlertType type, AdminMessage successMessage) {

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

        boolean monitoringEnabled = monitoringStateService.isMonitoringEnabled();

        String tdLibStatus = tdLibStatusService.isReady() ? "✅ підключено" : "⛔ не підключено";
        String activeMonitoringStatus = monitoringEnabled ? "✅ увімкнено" : "⛔ вимкнено";
        String controlModeStatus = monitoringStateService.isAutoMode() ? "АВТО" : "РУЧНЕ";
        int activeSourcesCount = monitoredSourceService.getActiveSources().size();

        StringBuilder message = new StringBuilder();

        message.append("""
                🤖 Статус бота

                🤖 Сервіс: ✅ працює
                📡 TDLib: %s
                📡 Активних джерел: %d

                🚨 Активний моніторинг: %s
                """.formatted(
                tdLibStatus,
                activeSourcesCount,
                activeMonitoringStatus
        ));

        if (monitoringEnabled) {
            message.append("Джерело активації: "
                + monitoringStateService.getMonitoringActivationSource()
                + "\n"
            );
        }

        message.append("""
                
                🔭 Моніторинг прогнозу: ✅ увімкнено

                ⚙️ Режим керування: %s
                """.formatted(
                controlModeStatus
        ));

        senderService.sendToChat(chatId, message.toString());
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

        senderService.sendToChat(chatId, "📡 Активні джерела (" + sources.size() + ")");

        for (int index = 0; index < sources.size(); index++) {

            MonitoredSource source = sources.get(index);

            String message = """
                    📡 Джерело %d/%d (активне)

                    Назва: %s
                    Chat ID: %s
                    """.formatted(
                    index + 1,
                    sources.size(),
                    source.title(),
                    source.chatId()
            );

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

        senderService.sendToChat(chatId, "🆕 Нові джерела (" + candidates.size() + ")");

        for (int index = 0; index < candidates.size(); index++) {

            UnknownSourceCandidate candidate = candidates.get(index);

            String message = """
                    📡 Джерело %d/%d (нове)

                    Назва: %s
                    Chat ID: %s

                    🕒 Остання активність: %s

                    💬 Повідомлення:
                    %s
                    """.formatted(
                    index + 1,
                    candidates.size(),
                    candidate.title(),
                    candidate.chatId(),
                    formatInstant(candidate.lastSeenAt()),
                    candidate.lastText()
            );

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

        senderService.sendToChat(chatId, "⛔ Ігноровані джерела (" + sources.size() + ")");

        for (int index = 0; index < sources.size(); index++) {

            MonitoredSource source = sources.get(index);

            String message = """
                    📡 Джерело %d/%d (ігнороване)

                    Назва: %s
                    Chat ID: %s
                    """.formatted(
                    index + 1,
                    sources.size(),
                    source.title(),
                    source.chatId()
            );

            InlineKeyboardMarkup keyboard = singleButtonKeyboard(
                "✅ Увімкнути моніторинг",
                SOURCE_ENABLE_CALLBACK + source.chatId()
            );

            senderService.sendToChatWithKeyboard(chatId, message, keyboard);
        }
    }

    private void enableSource(String adminChatId, Integer messageId, String callbackQueryId, String sourceId) {

        UnknownSourceCandidate candidate = unknownSourceCandidateService.findByChatId(sourceId);

        /*
         * NEW → ACTIVE
         */
        if (candidate != null) {

            boolean added = monitoredSourceService.addActiveSource(
                candidate.chatId(),
                candidate.title()
            );

            if (added) {

                unknownSourceCandidateService.remove(candidate.chatId());

                senderService.answerCallback(callbackQueryId, "Моніторинг увімкнено");

                String message = """
                        📡 Джерело (активне)

                        Назва: %s
                        Chat ID: %s
                        """.formatted(
                        candidate.title(),
                        candidate.chatId()
                );

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

        /*
         * IGNORED → ACTIVE
         */
        MonitoredSource source = monitoredSourceService.findByChatId(sourceId);

        if (source == null) {
            senderService.answerCallback(callbackQueryId, "Джерело не знайдено");
            return;
        }

        boolean enabled = monitoredSourceService.enableSource(sourceId);

        if (enabled) {

            senderService.answerCallback(callbackQueryId, "Моніторинг увімкнено");

            String message = """
                    📡 Джерело (активне)

                    Назва: %s
                    Chat ID: %s
                    """.formatted(
                    source.title(),
                    source.chatId()
            );

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

        /*
         * NEW → IGNORED
         */
        if (candidate != null) {

            boolean added = monitoredSourceService.addIgnoredSource(candidate.chatId(), candidate.title());

            if (added) {

                unknownSourceCandidateService.remove(candidate.chatId());

                senderService.answerCallback(callbackQueryId, "Джерело ігнорується");

                String message = """
                        📡 Джерело (ігнороване)

                        Назва: %s
                        Chat ID: %s
                        """.formatted(
                        candidate.title(),
                        candidate.chatId()
                );

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

        /*
         * ACTIVE → IGNORED
         */
        MonitoredSource source = monitoredSourceService.findByChatId(sourceId);

        if (source == null) {
            senderService.answerCallback(callbackQueryId, "Джерело не знайдено");
            return;
        }

        boolean ignored = monitoredSourceService.ignoreSource(sourceId);

        if (ignored) {

            senderService.answerCallback(callbackQueryId, "Моніторинг вимкнено");

            String message = """
                    📡 Джерело (ігнороване)

                    Назва: %s
                    Chat ID: %s
                    """.formatted(
                    source.title(),
                    source.chatId()
            );

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

    private String formatInstant(java.time.Instant instant) {

        if (instant == null) {
            return "невідомо";
        }

        return SOURCE_TIME_FORMATTER.format(instant.atZone(KYIV_ZONE));
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

    private void showDictionaryCategories(
        String chatId,
        Integer messageId,
        String callbackQueryId,
        DictionaryType type
    ) {

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