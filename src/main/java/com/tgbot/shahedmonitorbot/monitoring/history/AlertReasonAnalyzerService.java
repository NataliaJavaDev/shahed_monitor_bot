package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.admin.service.TargetAdminService;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReason;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonItem;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonResolverService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.processing.MessagePreprocessorService;
import com.tgbot.shahedmonitorbot.processing.PreprocessedMessage;
import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;
import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.alert.AlertDeliveryService;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class AlertReasonAnalyzerService {

    private static final Duration LOOKBACK =
            Duration.ofMinutes(15);

    private final TdLibHistoryRequestService historyService;
    private final MonitoredSourceService monitoredSourceService;
    private final AlertReasonResolverService alertReasonResolverService;
    private final MessagePreprocessorService messagePreprocessorService;
    private final TargetAdminService targetAdminService;
    private final AlertDeliveryService alertDeliveryService;

    public AlertReasonAnalyzerService(
            TdLibHistoryRequestService historyService,
            MonitoredSourceService monitoredSourceService,
            AlertReasonResolverService alertReasonResolverService,
            MessagePreprocessorService messagePreprocessorService,
            TargetAdminService targetAdminService,
            AlertDeliveryService alertDeliveryService
    ) {
        this.historyService = historyService;
        this.monitoredSourceService = monitoredSourceService;
        this.alertReasonResolverService = alertReasonResolverService;
        this.messagePreprocessorService = messagePreprocessorService;
        this.targetAdminService = targetAdminService;
        this.alertDeliveryService = alertDeliveryService;
    }

    public CompletableFuture<AlertReason> analyze() {
        return analyze(LOOKBACK);
    }

    public CompletableFuture<AlertReason> analyze(
            Duration lookback
    ) {

        List<String> chatIds = monitoredSourceService
                .getActiveSources()
                .stream()
                .map(MonitoredSource::chatId)
                .toList();

        return historyService
                .requestHistory(chatIds, lookback)
                .thenApply(this::analyzeHistory);
    }

    private AlertReason analyzeHistory(
            Map<String, List<TdHistoryMessage>> history
    ) {

        List<AlertReasonItem> items =
                new ArrayList<>();

        history.forEach((chatId, messages) -> {

            for (TdHistoryMessage historyMessage : messages) {

                System.out.println(historyMessage.text());

                PreprocessedMessage preprocessed =
                        messagePreprocessorService.preprocess(
                                historyMessage.text()
                        );

                System.out.println(preprocessed.cleanedText());

                if (preprocessed.cleanedText() == null) {
                    continue;
                }

                String normalizedText = preprocessed.cleanedText();

                targetAdminService.getTargets()
                        .stream()
                        .filter(normalizedText::contains)
                        .findFirst()
                        .ifPresent(target ->
                                items.add(
                                        new AlertReasonItem(
                                                targetAdminService.getCategory(target),
                                                java.util.Set.of(target)
                                        )
                                )
                        );
            }
        });

        return alertReasonResolverService.resolve(
                items
        );
    }
}