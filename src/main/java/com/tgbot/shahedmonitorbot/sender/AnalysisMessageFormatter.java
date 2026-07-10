package com.tgbot.shahedmonitorbot.sender;

import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;
import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import com.tgbot.shahedmonitorbot.processing.ThreatMatch;
import org.springframework.stereotype.Service;

@Service
public class AnalysisMessageFormatter {

    public String format(
            MessageAnalysis analysis,
            String sourceTitle,
            String chatId,
            String originalText
    ) {
        if (analysis.threatMatch() != null) {
            return formatThreatAnalysis(
                    analysis,
                    sourceTitle,
                    chatId,
                    originalText
            );
        }

        return formatMonitorAnalysis(
                analysis,
                sourceTitle,
                chatId,
                originalText
        );
    }

    private String formatMonitorAnalysis(
            MessageAnalysis analysis,
            String sourceTitle,
            String chatId,
            String originalText
    ) {
        MonitorMatch match = analysis.monitorMatch();

        return """
                🧠 Аналіз повідомлення

                📡 Джерело: %s
                🆔 Chat ID: %s

                📂 Тип збігу: %s

                🎯 Знайдена ціль: %s

                🧩 Категорія цілі:
                %s

                🧭 Напрямок: %s

                📍 Знайдена локація: %s

                🧩 Категорія локації:
                %s

                🔑 Ключ антидубля: %s

                🔄 Контекст використано:
                %s

                🧠 Intent:
                %s

                💬 Оригінальне повідомлення:

                %s
                """.formatted(
                sourceTitle,
                chatId,
                match.matchType().displayName(),
                formatNullable(match.matchedTarget()),
                formatNullable(match.targetCategory()),
                formatNullable(match.direction()),
                formatNullable(match.matchedLocation()),
                formatNullable(match.locationCategory()),
                formatNullable(analysis.deduplicationKey()),
                analysis.contextUsed() ? "Так" : "Ні",
                analysis.intent(),
                originalText
        );
    }

    private String formatThreatAnalysis(
            MessageAnalysis analysis,
            String sourceTitle,
            String chatId,
            String originalText
    ) {
        ThreatMatch threat = analysis.threatMatch();

        return """
                🧠 Аналіз повідомлення

                📡 Джерело: %s
                🆔 Chat ID: %s

                📂 Тип збігу: 🌐 Глобальна загроза

                ⚠️ Знайдена загроза: %s

                🧩 Категорія загрози:
                %s

                🔑 Ключ антидубля: %s

                🔄 Контекст використано:
                Ні

                🧠 Intent:
                %s

                💬 Оригінальне повідомлення:

                %s
                """.formatted(
                sourceTitle,
                chatId,
                formatNullable(threat.matchedThreat()),
                formatNullable(threat.threatCategory()),
                formatNullable(analysis.deduplicationKey()),
                analysis.intent(),
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