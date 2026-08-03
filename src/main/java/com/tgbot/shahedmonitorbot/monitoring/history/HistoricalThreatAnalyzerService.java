package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.tdlib.history.TdHistoryMessage;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class HistoricalThreatAnalyzerService {

    private static final Duration LOOKBACK =
            Duration.ofMinutes(15);

    private final TdLibHistoryRequestService historyService;
    private final MonitoredSourceService monitoredSourceService;

    public HistoricalThreatAnalyzerService(
            TdLibHistoryRequestService historyService,
            MonitoredSourceService monitoredSourceService
    ) {
        this.historyService = historyService;
        this.monitoredSourceService = monitoredSourceService;
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

            System.out.println();
            System.out.println("====================================");
            System.out.println("CHAT: " + chatId);
            System.out.println("====================================");

            for (TdHistoryMessage message : messages) {

                System.out.println(message.dateTime());
                System.out.println(message.text());
                System.out.println("------------------------------------");
            }
        });
    }
}