package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.context.EventContextService;
import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import org.springframework.stereotype.Service;

@Service
public class MessageAnalysisService {

    private final MonitorFilterService monitorFilterService;
    private final DeduplicationService deduplicationService;
    private final EventContextService eventContextService;

    public MessageAnalysisService(
            MonitorFilterService monitorFilterService,
            DeduplicationService deduplicationService,
            EventContextService eventContextService
    ) {
        this.monitorFilterService = monitorFilterService;
        this.deduplicationService = deduplicationService;
        this.eventContextService = eventContextService;
    }

    public MessageAnalysis analyze(String text) {

        return monitorFilterService.findMatch(text)
                .map(match -> {

                    String deduplicationKey =
                            deduplicationService.buildDeduplicationKey(match);

                    boolean duplicate =
                            deduplicationService.isDuplicate(match);

                    if (!duplicate) {
                        eventContextService.save(match);
                    }

                    return new MessageAnalysis(
                            match,
                            duplicate,
                            false,
                            deduplicationKey
                    );
                })
                .orElse(null);
    }
}