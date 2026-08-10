package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.context.EventContextService;
import com.tgbot.shahedmonitorbot.deduplication.DeduplicationService;
import com.tgbot.shahedmonitorbot.monitoring.MonitoringStateService;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MessageAnalysisService {

    private final MonitorFilterService monitorFilterService;
    private final ContextResolverService contextResolverService;
    private final DeduplicationService deduplicationService;
    private final EventContextService eventContextService;
    private final MessageIntentDetectorService messageIntentDetectorService;
    private final ThreatDetectorService threatDetectorService;
    private final MessagePreprocessorService messagePreprocessorService;
    private final MonitoringStateService monitoringStateService;

    public MessageAnalysisService(
            MonitorFilterService monitorFilterService,
            ContextResolverService contextResolverService,
            DeduplicationService deduplicationService,
            EventContextService eventContextService,
            MessageIntentDetectorService messageIntentDetectorService,
            ThreatDetectorService threatDetectorService,
            MessagePreprocessorService messagePreprocessorService,
            MonitoringStateService monitoringStateService
    ) {
        this.monitorFilterService = monitorFilterService;
        this.contextResolverService = contextResolverService;
        this.deduplicationService = deduplicationService;
        this.eventContextService = eventContextService;
        this.messageIntentDetectorService = messageIntentDetectorService;
        this.threatDetectorService = threatDetectorService;
        this.messagePreprocessorService = messagePreprocessorService;
        this.monitoringStateService = monitoringStateService;
    }

    public MessageAnalysis analyze(String chatId, String text) {
        PreprocessedMessage preprocessed =
                messagePreprocessorService.preprocess(text);

        if (preprocessed.cleanedText() == null) {
            return null;
        }

        String cleanedText = preprocessed.cleanedText();

        MessageIntent intent =
                messageIntentDetectorService.detect(cleanedText);

        if (intent == MessageIntent.THREAT_DETECTED) {

            if (!monitoringStateService.isMonitoringEnabled()) {
                return analyzeThreat(cleanedText, intent);
            }

            return null;
        }

        if (preprocessed.tooLongForLocalAnalysis()) {
            return null;
        }

        Optional<MonitorMatch> match =
                monitorFilterService.findMatch(cleanedText);

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
                        finalContextRestored,
                        cleanedText
                ))
                .orElse(null);
    }

    private MessageAnalysis analyzeMatch(
            String chatId,
            MonitorMatch initialMatch,
            MessageIntent intent,
            boolean contextRestored,
            String text
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
                null,
                finalIntent,
                duplicate,
                resolution.contextUsed() || contextRestored,
                deduplicationKey,
                text
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
                || intent == MessageIntent.STATUS_UPDATE
                || intent == MessageIntent.THREAT_DETECTED) {
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

    private MessageAnalysis analyzeThreat(
            String text,
            MessageIntent intent
    ) {
        return threatDetectorService.findThreat(text)
                .map(threatMatch -> {
                    String deduplicationKey =
                            deduplicationService.buildThreatDeduplicationKey(threatMatch);

                    boolean duplicate =
                            deduplicationService.isDuplicate(threatMatch);

                    return new MessageAnalysis(
                            null,
                            threatMatch,
                            intent,
                            duplicate,
                            false,
                            deduplicationKey,
                            text
                    );
                })
                .orElse(null);
    }
}