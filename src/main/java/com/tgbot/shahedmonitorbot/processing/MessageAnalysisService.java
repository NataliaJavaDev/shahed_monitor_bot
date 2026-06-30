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
    private final MessageIntentDetectorService messageIntentDetectorService;

    public MessageAnalysisService(
            MonitorFilterService monitorFilterService,
            ContextResolverService contextResolverService,
            DeduplicationService deduplicationService,
            EventContextService eventContextService,
            MessageIntentDetectorService messageIntentDetectorService
    ) {
        this.monitorFilterService = monitorFilterService;
        this.contextResolverService = contextResolverService;
        this.deduplicationService = deduplicationService;
        this.eventContextService = eventContextService;
        this.messageIntentDetectorService = messageIntentDetectorService;
    }

    public MessageAnalysis analyze(String text) {
        MessageIntent intent = messageIntentDetectorService.detect(text);

        return monitorFilterService.findMatch(text)
                .map(match -> analyzeMatch(match, intent))
                .orElse(null);
    }

    private MessageAnalysis analyzeMatch(MonitorMatch initialMatch, MessageIntent intent) {
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
                intent,
                duplicate,
                resolution.contextUsed(),
                deduplicationKey
        );
    }
}