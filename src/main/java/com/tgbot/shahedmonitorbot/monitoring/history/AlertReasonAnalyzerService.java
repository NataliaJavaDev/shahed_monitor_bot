package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.admin.service.TargetAdminService;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReason;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonItem;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonResolverService;
import com.tgbot.shahedmonitorbot.processing.MessagePreprocessorService;
import com.tgbot.shahedmonitorbot.processing.PreprocessedMessage;
import com.tgbot.shahedmonitorbot.processing.RecentMessageCacheService;
import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;
import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class AlertReasonAnalyzerService {

    private static final Duration LOOKBACK = Duration.ofMinutes(30);

    private final AlertReasonResolverService alertReasonResolverService;
    private final MessagePreprocessorService messagePreprocessorService;
    private final TargetAdminService targetAdminService;
    private final AlertDeliveryService alertDeliveryService;
    private final RecentMessageCacheService recentMessageCacheService;

    public AlertReasonAnalyzerService(
            AlertReasonResolverService alertReasonResolverService,
            MessagePreprocessorService messagePreprocessorService,
            TargetAdminService targetAdminService,
            AlertDeliveryService alertDeliveryService,
            RecentMessageCacheService recentMessageCacheService
    ) {
        this.alertReasonResolverService = alertReasonResolverService;
        this.messagePreprocessorService = messagePreprocessorService;
        this.targetAdminService = targetAdminService;
        this.alertDeliveryService = alertDeliveryService;
        this.recentMessageCacheService = recentMessageCacheService;
    }

    public AlertReason analyze() {
        return analyze(LOOKBACK);
    }

    public AlertReason analyze(Duration lookback) {

        return analyzeHistory(
            recentMessageCacheService.getHistory(lookback)
        );
    }

    private AlertReason analyzeHistory(
            Map<String, List<TdHistoryMessage>> history
    ) {

        List<AlertReasonItem> items = new ArrayList<>();

        history.values().forEach(messages -> {

            for (TdHistoryMessage historyMessage : messages) {

                PreprocessedMessage preprocessed =
                        messagePreprocessorService.preprocess(
                                historyMessage.text()
                        );

                if (preprocessed.cleanedText() == null) {
                    continue;
                }

                String normalizedText = preprocessed.cleanedText();

                alertDeliveryService.send("""
NORMALIZED

%s
""".formatted(normalizedText));

                targetAdminService.getTargets()
                    .stream()
                    .filter(normalizedText::contains)
                    .findFirst()
                    .ifPresent(target ->
                        items.add(
                            new AlertReasonItem(
                                targetAdminService.getDisplayName(
                                        targetAdminService.getCategory(target)
                                ),
                                java.util.Set.of(target)
                            )
                        )
                    );
            }
        });

        AlertReason reason = alertReasonResolverService.resolve(items);

        return reason;
    }
}