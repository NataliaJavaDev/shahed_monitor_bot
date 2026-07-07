package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.context.EventContextService;
import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import org.springframework.stereotype.Service;

import java.util.Optional;

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

        Optional<MonitorMatch> match = monitorFilterService.findMatch(text);

        boolean contextRestored = false;

        if (match.isEmpty() && requiresContext(intent)) {
            match = eventContextService.getLastEvent();
            contextRestored = match.isPresent();
        }

        boolean finalContextRestored = contextRestored;
        MessageIntent finalIntent = intent;

        return match
                .map(currentMatch -> analyzeMatch(
                        currentMatch,
                        finalIntent,
                        finalContextRestored
                ))
                .orElse(null);
    }

    private MessageAnalysis analyzeMatch(
            MonitorMatch initialMatch,
            MessageIntent intent,
            boolean contextRestored
    ) {
        MessageIntent finalIntent =
                resolveFinalIntent(intent, initialMatch, contextRestored);

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
                finalIntent,
                duplicate,
                resolution.contextUsed() || contextRestored,
                deduplicationKey
        );
    }

    private boolean requiresContext(MessageIntent intent) {
        return switch (intent) {
            case COUNT_UPDATE,
            ROUTE_UPDATE -> true;
            default -> false;
        };
    }

    private MessageIntent resolveFinalIntent(
            MessageIntent detectedIntent,
            MonitorMatch match,
            boolean contextRestored
    ) {
        if (contextRestored) {
            return detectedIntent;
        }

        if (match.matchType() == MatchType.TARGET_AND_LOCATION) {
            return MessageIntent.NEW_EVENT;
        }

        return detectedIntent;
    }
}