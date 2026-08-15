package com.tgbot.shahedmonitorbot.telegram;

import com.tgbot.shahedmonitorbot.admin.command.AdminCommandHandler;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class AdminTelegramBot implements LongPollingUpdateConsumer {

    private static final Logger log = LoggerFactory.getLogger(AdminTelegramBot.class);

    private final AdminCommandHandler adminCommandHandler;
    private final AppProperties properties;

    public AdminTelegramBot(
        AdminCommandHandler adminCommandHandler,
        AppProperties properties
    ) {
        this.adminCommandHandler = adminCommandHandler;
        this.properties = properties;
    }

    @PostConstruct
    public void registerBot() throws Exception {

        TelegramBotsLongPollingApplication application = new TelegramBotsLongPollingApplication();

        application.registerBot(properties.telegram().botToken(),this);
    }

    @Override
    public void consume(List<Update> updates) {

        for (Update update : updates) {
            handleUpdate(update);
        }
    }

    private void handleUpdate(Update update) {

        if (update.hasCallbackQuery()) {
            handleCallbackQuery(update.getCallbackQuery());
            return;
        }

        if (!update.hasMessage()) {
            return;
        }

        if (!update.getMessage().hasText()) {
            return;
        }

        Long userId = update.getMessage().getFrom().getId();
        Long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText();

        log.debug("New update from user {} in chat {}: {}",
            userId,
            chatId,
            text
        );

        if (chatId.toString().equals(properties.telegram().targetChannelId())) {
            log.debug("Ignored message from target channel/group: {}", chatId);
            return;
        }

        adminCommandHandler.handle(userId, chatId.toString(), text);
    }

    private void handleCallbackQuery(
        CallbackQuery callbackQuery
    ) {

        if (callbackQuery.getMessage() == null) {
            return;
        }

        Long userId = callbackQuery.getFrom().getId();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        Integer messageId = callbackQuery.getMessage().getMessageId();
        String callbackData = callbackQuery.getData();

        if (callbackData == null || callbackData.isBlank()) {
            return;
        }

        log.debug("New callback from user {} in chat {}: {}", userId, chatId, callbackData);

        adminCommandHandler.handleCallback(
            userId,
            chatId,
            messageId,
            callbackQuery.getId(),
            callbackData
        );
    }
}