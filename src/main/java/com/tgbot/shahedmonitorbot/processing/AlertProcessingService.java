package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import com.tgbot.shahedmonitorbot.filter.KeywordMatcherService;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class AlertProcessingService {

    private final KeywordMatcherService keywordMatcherService;
    private final DeduplicationService deduplicationService;
    private final AlertMessageFormatter formatter;
    private final TelegramSenderService senderService;

    public AlertProcessingService(
            KeywordMatcherService keywordMatcherService,
            DeduplicationService deduplicationService,
            AlertMessageFormatter formatter,
            TelegramSenderService senderService
    ) {
        this.keywordMatcherService = keywordMatcherService;
        this.deduplicationService = deduplicationService;
        this.formatter = formatter;
        this.senderService = senderService;
    }

    public void process(String sourceName, String text) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (!keywordMatcherService.isRelevant(text)) {
            return;
        }

        if (deduplicationService.isDuplicate(text)) {
            return;
        }

        String message = formatter.format(sourceName, text);
        senderService.send(message);
    }
}