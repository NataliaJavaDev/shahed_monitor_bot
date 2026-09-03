package com.tgbot.shahedmonitorbot.sender;

import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageFormatter {

    public String format(
        MessageAnalysis analysis,
        String sourceTitle,
        String chatId,
        String originalText
    ) {

        MonitorMatch match = analysis.monitorMatch();

        return """
                🚨 Моніторинг

                📡 Джерело: %s

                🎯 Ціль: %s
                🧭 Напрямок: %s
                📍 Локація: %s

                💬 Оригінальне повідомлення:

                %s
                """.formatted(
                sourceTitle,
                formatNullable(match.targetCategory()),
                formatNullable(match.direction()),
                formatNullable(match.locationCategory()),
                originalText
        );
    }

    public String formatDebug(
        MessageAnalysis analysis,
        String sourceTitle,
        String originalText
    ) {
        
        MonitorMatch match = analysis.monitorMatch();

        return """
                🧪 АНАЛІЗ

                📡 Джерело: %s

                🧠 Intent: %s
                🔁 Duplicate: %s

                🌐 Global threat: %s
                🔮 Forecast: %s

                🎯 Ціль: %s
                🧭 Напрямок: %s
                📍 Локація: %s

                💬 Оригінальне повідомлення:

                %s
                """.formatted(
                sourceTitle,
                analysis.intent(),
                analysis.duplicate(),
                formatNullable(
                        analysis.globalThreatMatch() == null
                                ? null
                                : analysis.globalThreatMatch().matchedMarker()
                ),
                formatNullable(
                        analysis.forecastMatch() == null
                                ? null
                                : analysis.forecastMatch().matchedMarker()
                ),
                formatNullable(match == null ? null : match.targetCategory()),
                formatNullable(match == null ? null : match.direction()),
                formatNullable(match == null ? null : match.locationCategory()),
                originalText
        );
    }

    private String formatNullable(String value) {

        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }
}