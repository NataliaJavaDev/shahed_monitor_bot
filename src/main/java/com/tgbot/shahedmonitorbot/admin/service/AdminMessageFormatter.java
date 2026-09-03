package com.tgbot.shahedmonitorbot.admin.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

import com.tgbot.shahedmonitorbot.monitoring.source.UnknownSourceCandidate;

@Service
public class AdminMessageFormatter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm").withZone(ZoneId.systemDefault());

    public String formatBotStatus(
        boolean tdLibReady,
        int activeSourcesCount,
        boolean monitoringEnabled,
        String activationSource,
        boolean isAutoMode
    ) {
        String tdLibStatus = tdLibReady ? "✅ підключено" : "⛔ не підключено";
        String activeMonitoringStatus = monitoringEnabled ? "✅ увімкнено" : "⛔ вимкнено";
        String controlModeStatus = isAutoMode ? "АВТО" : "РУЧНЕ";

        StringBuilder message = new StringBuilder();

        message.append("""
            🤖 Статус бота

            🤖 Сервіс: ✅ працює
            📡 TDLib: %s
            📡 Активних джерел: %d

            🚨 Активний моніторинг: %s
            """.formatted(tdLibStatus, activeSourcesCount, activeMonitoringStatus));

        if (monitoringEnabled) {
            message.append("Джерело активації: ")
                .append(activationSource != null ? activationSource : "невідомо")
                .append("\n");
        }

        message.append("""
                
            🔭 Моніторинг прогнозу: ✅ увімкнено

            ⚙️ Режим керування: %s
            """.formatted(controlModeStatus));

        return message.toString();
    }

    public String formatActiveSourcesHeader(int count) {
        return "📡 Активні джерела (" + count + ")";
    }

    public String formatNewSourcesHeader(int count) {
        return "🆕 Нові джерела (" + count + ")";
    }

    public String formatIgnoredSourcesHeader(int count) {
        return "⛔ Ігноровані джерела (" + count + ")";
    }

    public String formatSourceCard(String title, String chatId, String statusLabel, Integer index, Integer total) {
        
        String header = (index != null && total != null)
            ? "📡 Джерело %d/%d (%s)".formatted(index, total, statusLabel)
            : "📡 Джерело (%s)".formatted(statusLabel);

        return """
            %s

            Назва: %s
            Chat ID: %s
            """.formatted(header, title, chatId);
    }

    public String formatNewSourceCandidateCard(UnknownSourceCandidate candidate, int index, int total) {
        return """
            📡 Джерело %d/%d (нове)

            Назва: %s
            Chat ID: %s

            🕒 Остання активність: %s

            💬 Повідомлення:
            %s
            """.formatted(
            index,
            total,
            candidate.title(),
            candidate.chatId(),
            formatInstant(candidate.lastSeenAt()),
            candidate.lastText()
        );
    }

    public String formatInstant(Instant instant) {
        return instant != null ? DATE_FORMATTER.format(instant) : "—";
    }
}