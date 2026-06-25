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
        System.out.println("PROCESSING TEXT: " + text);

        if (text == null || text.isBlank()) {
            System.out.println("TEXT IS EMPTY");
            return;
        }

        var match = monitorFilterService.findMatch(text);
        System.out.println("IS RELEVANT: " + match.isPresent());

        if (match.isEmpty()) {
            return;
        }

        boolean duplicate = deduplicationService.isDuplicate(match.get());
        System.out.println("IS DUPLICATE: " + duplicate);

        if (duplicate) {
            return;
        }

        String message = formatter.format(sourceName, text);
        System.out.println("FORMATTED MESSAGE: " + message);

        senderService.send(message);
    }
}