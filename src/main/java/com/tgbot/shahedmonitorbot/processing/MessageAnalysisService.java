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

    public MessageAnalysis analyze(String chatId, String text) {
        MessageIntent intent = messageIntentDetectorService.detect(text);

        Optional<MonitorMatch> match = monitorFilterService.findMatch(text);

        boolean contextRestored = false;

        if (match.isEmpty() && requiresContext(intent)) {
            match = eventContextService.getContext(chatId);
            contextRestored = match.isPresent();
        }

        boolean finalContextRestored = contextRestored;
        MessageIntent finalIntent = intent;

        return match
                .map(currentMatch -> analyzeMatch(
                        chatId,
                        currentMatch,
                        finalIntent,
                        finalContextRestored
                ))
                .orElse(null);
    }

    private MessageAnalysis analyzeMatch(
            String chatId,
            MonitorMatch initialMatch,
            MessageIntent intent,
            boolean contextRestored
    ) {
        MessageIntent finalIntent =
                resolveFinalIntent(intent, initialMatch, contextRestored);

        ContextResolution resolution =
                contextResolverService.resolve(chatId, initialMatch);

        MonitorMatch finalMatch = resolution.match();

        String deduplicationKey =
                deduplicationService.buildDeduplicationKey(finalMatch);

        boolean duplicate =
                deduplicationService.isDuplicate(finalMatch);

        if (!duplicate && shouldSaveContext(finalMatch, finalIntent)) {
            eventContextService.saveContext(chatId, finalMatch);
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

    private boolean shouldSaveContext(
            MonitorMatch match,
            MessageIntent intent
    ) {
        if (match == null) {
            return false;
        }

        if (intent == MessageIntent.ATTENTION
                || intent == MessageIntent.COUNT_UPDATE
                || intent == MessageIntent.STATUS_UPDATE) {
            return false;
        }

        return switch (match.matchType()) {
            case TARGET_AND_LOCATION,
                 DIRECTION_AND_LOCATION -> true;
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