package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.monitoring.history.debug.HistoricalThreatDebugService;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysisService;
import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class HistoricalThreatAnalyzerService {

    private static final Duration LOOKBACK =
            Duration.ofMinutes(15);

    private final TdLibHistoryRequestService historyService;
    private final MonitoredSourceService monitoredSourceService;
    private final MessageAnalysisService messageAnalysisService;
    private final HistoricalThreatBuilder historicalThreatBuilder;
    private final HistoricalThreatDebugService debugService;

    public HistoricalThreatAnalyzerService(
            TdLibHistoryRequestService historyService,
            MonitoredSourceService monitoredSourceService,
            MessageAnalysisService messageAnalysisService,
            HistoricalThreatBuilder historicalThreatBuilder,
            HistoricalThreatDebugService debugService
    ) {
        this.historyService = historyService;
        this.monitoredSourceService = monitoredSourceService;
        this.messageAnalysisService = messageAnalysisService;
        this.historicalThreatBuilder = historicalThreatBuilder;
         this.debugService = debugService;
    }

    public CompletableFuture<Void> analyze() {
        return analyze(LOOKBACK);
    }

    public CompletableFuture<Void> analyze(
        Duration lookback
    ) {

        List<String> chatIds = monitoredSourceService
                .getActiveSources()
                .stream()
                .map(MonitoredSource::chatId)
                .toList();

        return historyService
                .requestHistory(chatIds, lookback)
                .thenAccept(this::analyzeHistory);
    }

    private void analyzeHistory(
            Map<String, List<TdHistoryMessage>> history
    ) {

        history.forEach((chatId, messages) -> {

            List<MessageAnalysis> analyses =
                    new ArrayList<>();

            for (TdHistoryMessage historyMessage : messages) {

                MessageAnalysis analysis =
                        messageAnalysisService.analyze(
                                chatId,
                                historyMessage.text()
                        );

                if (analysis != null) {
                    analyses.add(analysis);
                }
            }

            HistoricalThreatState state =
                    historicalThreatBuilder.build(analyses);

            MonitoredSource source =
                    monitoredSourceService.findByChatId(chatId);

            debugService.append(
                    source != null ? source.title() : chatId,
                    state
            );
        });
    }
}