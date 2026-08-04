package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HistoricalThreatBuilder {

    public HistoricalThreatState build(
            List<MessageAnalysis> analyses
    ) {

        HistoricalThreatState state =
                new HistoricalThreatState();
                
        state.setTotalMessages(analyses.size());
        state.setAnalyzedMessages(analyses.size());

        for (MessageAnalysis analysis : analyses) {
            update(state, analysis);
        }

        return state;
    }

    private void update(
            HistoricalThreatState state,
            MessageAnalysis analysis
    ) {

        state.getTimeline().add(analysis);

        switch (analysis.intent()) {

            case THREAT_DETECTED -> {

                state.setForecast(analysis);
                state.setCurrentThreat(analysis);
            }

            case ROUTE_UPDATE ->
                    state.setLatestRoute(analysis);

            case COUNT_UPDATE ->
                    state.setLatestCount(analysis);

            case ATTENTION ->
                    state.setLatestAttention(analysis);

            default -> {
            }
        }
    }
}