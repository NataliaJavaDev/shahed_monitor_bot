package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReason;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonItem;
import org.springframework.stereotype.Component;

@Component
public class ManualAlertMessageFormatter {

    public String format(
            ManualAlertType type,
            AlertReason reason
    ) {

        return switch (type) {

            case ALERT -> """
                    🚨 ПОВІТРЯНА ТРИВОГА
                    
                    %s
                    Негайно пройдіть в укриття.
                    """
                    .formatted(buildReason(reason));

            case HIGH_RISK -> """
                    ⚠️ ПІДВИЩЕНА НЕБЕЗПЕКА
                    
                    Слідкуйте за подальшими повідомленнями.
                    """;

            case ALL_CLEAR -> """
                    ✅ ВІДБІЙ ТРИВОГИ
                    
                    Загрозу скасовано.
                    """;
        };
    }

    private String buildReason(AlertReason reason) {

        if (reason == null || reason.items().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Причина:")
                .append(System.lineSeparator());

        for (AlertReasonItem item : reason.items()) {

            sb.append("• ")
                    .append(item.category());

            if (!item.threats().isEmpty()) {
                sb.append(": ")
                        .append(String.join(", ", item.threats()));
            }

            sb.append(System.lineSeparator());
        }

        sb.append(System.lineSeparator());

        return sb.toString();
    }
}