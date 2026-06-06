package com.tgbot.shahedmonitorbot.alertapi.formatter;

import com.tgbot.shahedmonitorbot.alertapi.model.ApiAlertStatus;
import org.springframework.stereotype.Component;

@Component
public class ApiAlertStatusFormatter {

    public String format(ApiAlertStatus status) {
        StringBuilder builder = new StringBuilder();

        builder.append("📡 Статус тривоги\n\n");

        builder.append("Звичайна тривога по району: ")
                .append(status.districtAlertActive() ? "є" : "немає")
                .append("\n");

        builder.append("Підвищена небезпека: ")
                .append(status.activeDangerRegionNames().isEmpty() ? "немає" : "є")
                .append("\n");

        if (!status.activeDangerRegionNames().isEmpty()) {
            builder.append("\nАктивні близькі регіони:\n");

            for (String regionName : status.activeDangerRegionNames()) {
                builder.append("• ").append(regionName).append("\n");
            }
        }

        builder.append("\nОстання перевірка API: ")
                .append(status.checkedAt());

        return builder.toString();
    }
}