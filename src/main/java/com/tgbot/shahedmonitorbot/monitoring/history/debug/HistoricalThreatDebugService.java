package com.tgbot.shahedmonitorbot.monitoring.history.debug;

import com.tgbot.shahedmonitorbot.monitoring.history.HistoricalThreatState;
import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class HistoricalThreatDebugService {

    private static final DateTimeFormatter FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Path file =
            Path.of("exports")
                    .resolve("analysis")
                    .resolve("history-analysis.txt");

    public synchronized void append(
            String sourceName,
            HistoricalThreatState state
    ) {

        try {

            Files.createDirectories(file.getParent());

            Files.writeString(
                    file,
                    build(sourceName, state),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );

        } catch (IOException e) {

            System.out.println(
                    "Failed to write history analysis: "
                            + e.getMessage()
            );
        }
    }

    private String build(
            String sourceName,
            HistoricalThreatState state
    ) {

        return """
============================================================
%s
SOURCE : %s
Messages : %d
Analyzed: %d
============================================================

FORECAST
%s

CURRENT
%s

ROUTE
%s

COUNT
%s

ATTENTION
%s

---------------- TIMELINE ----------------

%s

============================================================


"""
                .formatted(
                        LocalDateTime.now().format(FORMAT),
                        sourceName,
                        state.getTotalMessages(),
                        state.getAnalyzedMessages(),
                        format(state.getForecast()),
                        format(state.getCurrentThreat()),
                        format(state.getLatestRoute()),
                        format(state.getLatestCount()),
                        format(state.getLatestAttention()),
                        buildTimeline(state)
                );
    }

    private String buildTimeline(
            HistoricalThreatState state
    ) {

        StringBuilder sb = new StringBuilder();

        for (MessageAnalysis analysis : state.getTimeline()) {

            sb.append("- ")
                    .append(analysis.intent());

            if (analysis.monitorMatch() != null) {

                sb.append(" | ")
                        .append(analysis.monitorMatch());
            }

            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }

    private String format(
            MessageAnalysis analysis
    ) {

        if (analysis == null) {
            return "-";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Intent : ")
                .append(analysis.intent())
                .append(System.lineSeparator());

        if (analysis.monitorMatch() != null) {

            sb.append("Match  : ")
                    .append(analysis.monitorMatch())
                    .append(System.lineSeparator());
        }

        sb.append("Text   : ")
                .append(analysis.originalMessage());

        return sb.toString();
    }
}