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

    private String formatNullable(String value) {
        if (value == null || value.isBlank()) {
            return "-";
        }

        return value;
    }
}