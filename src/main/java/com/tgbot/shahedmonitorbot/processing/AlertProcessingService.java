package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import com.tgbot.shahedmonitorbot.sender.TelegramSenderService;
import org.springframework.stereotype.Service;

@Service
public class AlertProcessingService {

    private final MonitorFilterService monitorFilterService;
    private final DeduplicationService deduplicationService;
    private final AlertMessageFormatter formatter;
    private final TelegramSenderService senderService;

    public AlertProcessingService(
        MonitorFilterService monitorFilterService,
        DeduplicationService deduplicationService,
        AlertMessageFormatter formatter,
        TelegramSenderService senderService
    ) {
        this.monitorFilterService = monitorFilterService;
        this.deduplicationService = deduplicationService;
        this.formatter = formatter;
        this.senderService = senderService;
    }

    public void process(String sourceName, String text) {

        if (text == null || text.isBlank()) {
            return;
        }

        var match = monitorFilterService.findMatch(text);

        if (match.isEmpty()) {
            return;
        }

        boolean duplicate = deduplicationService.isDuplicate(match.get());

        if (duplicate) {
            return;
        }

        String message = formatter.format(sourceName, text);

        senderService.send(message);
    }
}