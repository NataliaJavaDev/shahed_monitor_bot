package com.tgbot.shahedmonitorbot.alertapi.formatter;

import com.tgbot.shahedmonitorbot.alertapi.model.ApiAlertStatus;
import com.tgbot.shahedmonitorbot.alertapi.model.ApiCheckHealth;
import com.tgbot.shahedmonitorbot.alertapi.model.DangerLevel;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ApiAlertStatusFormatter {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    /** Сумісність зі старими викликами: без інформації про помилки. */
    public String format(ApiAlertStatus status) {
        return format(status, null);
    }

    public String format(ApiAlertStatus status, ApiCheckHealth health) {

        StringBuilder builder = new StringBuilder();

        builder.append("📡 Статус тривоги\n\n");

        boolean noDataYet = health != null && health.lastSuccessAt() == null;

        if (noDataYet) {
            builder.append("⏳ Ще не було жодної успішної перевірки API\n");
        } else {
            builder.append("Тривога по району: ")
                .append(describeLevel(status.districtLevel())).append("\n");

            builder.append("Підвищена небезпека: ")
                .append(status.highRiskActive() ? "‼️ є" : "немає данних").append("\n");
        }

        appendHealth(builder, health, noDataYet);

        LocalDateTime lastSuccess = health != null
            ? health.lastSuccessAt()
            : status.checkedAt();

        if (lastSuccess != null) {
            builder.append("\nОстання успішна перевірка API: ")
                .append(lastSuccess.format(DATE_TIME_FORMATTER));
        }
        return builder.toString();
    }

    private String describeLevel(DangerLevel level) {
        return switch (level) {
            case RED -> "🔴 є, червоний рівень";
            case YELLOW -> "🟡 є, жовтий рівень";
            case NONE -> "🟢 немає";
        };
    }

    private void appendHealth(StringBuilder builder, ApiCheckHealth health, boolean noDataYet) {

        if (health == null) {
            return;
        }

        if (health.consecutiveFailures() > 0) {

            builder.append("\n⚠️ Помилка API (поспіль: ")
                .append(health.consecutiveFailures()).append(")\n");

            builder.append("Помилка: ")
                .append(health.lastError()).append("\n");

            builder.append("Час: ")
                .append(health.lastErrorAt().format(DATE_TIME_FORMATTER)).append("\n");

            if (!noDataYet) {
                builder.append("Дані вище можуть бути застарілими.\n");
            }
            return;
        }

        if (health.lastError() != null) {

            builder.append("\nОстання помилка (вже відновлено): ")
                .append(health.lastError()).append(" (")
                .append(health.lastErrorAt().format(DATE_TIME_FORMATTER)).append(")\n");
        }
    }
}