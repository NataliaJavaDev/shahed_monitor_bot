package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import com.tgbot.shahedmonitorbot.processing.RecentMessageCacheService;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RecentHistoryBootstrapService {

    private static final Duration LOOKBACK =
            Duration.ofHours(2);

    private final TdLibHistoryRequestService historyService;
    private final MonitoredSourceService monitoredSourceService;
    private final RecentMessageCacheService recentMessageCacheService;

    private volatile boolean initialized = false;

    public RecentHistoryBootstrapService(
            TdLibHistoryRequestService historyService,
            MonitoredSourceService monitoredSourceService,
            RecentMessageCacheService recentMessageCacheService
    ) {
        this.historyService = historyService;
        this.monitoredSourceService = monitoredSourceService;
        this.recentMessageCacheService = recentMessageCacheService;
    }

    public synchronized void bootstrap() {

        List<String> chatIds = monitoredSourceService
                .getActiveSources()
                .stream()
                .map(MonitoredSource::chatId)
                .toList();

        historyService
                .requestHistory(chatIds, LOOKBACK)
                .thenAccept(history -> {

                    history.forEach((chatId, messages) -> {

                        for (var message : messages) {
                            recentMessageCacheService.add(chatId, message);
                        }
                    });
                    
                    initialized = true;
                })
                .exceptionally(ex -> {
                    ex.printStackTrace();
                    return null;
                });
    }
}