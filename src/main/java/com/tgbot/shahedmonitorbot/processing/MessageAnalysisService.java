package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.context.EventContextService;
import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import org.springframework.stereotype.Service;

@Service
public class MessageAnalysisService {

    private final MonitorFilterService monitorFilterService;
    private final ContextResolverService contextResolverService;
    private final DeduplicationService deduplicationService;
    private final EventContextService eventContextService;

    public MessageAnalysisService(
            MonitorFilterService monitorFilterService,
            ContextResolverService contextResolverService,
            DeduplicationService deduplicationService,
            EventContextService eventContextService
    ) {
        this.monitorFilterService = monitorFilterService;
        this.contextResolverService = contextResolverService;
        this.deduplicationService = deduplicationService;
        this.eventContextService = eventContextService;
    }

    public MessageAnalysis analyze(String text) {
        return monitorFilterService.findMatch(text)
                .map(this::analyzeMatch)
                .orElse(null);
    }

    private MessageAnalysis analyzeMatch(MonitorMatch initialMatch) {
        ContextResolution resolution =
                contextResolverService.resolve(initialMatch);

        MonitorMatch finalMatch = resolution.match();

        String deduplicationKey =
                deduplicationService.buildDeduplicationKey(finalMatch);

        boolean duplicate =
                deduplicationService.isDuplicate(finalMatch);

        if (!duplicate) {
            eventContextService.save(finalMatch);
        }

        return new MessageAnalysis(
                finalMatch,
                duplicate,
                resolution.contextUsed(),
                deduplicationKey
        );
    }
}