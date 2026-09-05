package com.tgbot.shahedmonitorbot.manualalert;

import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReason;
import com.tgbot.shahedmonitorbot.monitoring.reason.AlertReasonItem;
import org.springframework.stereotype.Component;

@Component
public class ManualAlertMessageFormatter {

    public String format(
            AlertType type,
            AlertReason reason
    ) {
        return switch (type) {

            case ALERT -> """
                    \u2063
                    🚨 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
                    %s
                    Негайно пройдіть в укриття.
                    """
                    .formatted(buildReason(reason));

            case HIGH_RISK -> """
                    \u2063
                    ‼️ ПІДВИЩЕНА НЕБЕЗПЕКА НА БІЛОЦЕРКІВЩИНІ
                    
                    Негайно пройдіть в укриття.
                    """;

            case ALL_CLEAR -> """
                    \u2063
                    ✅ ВІДБІЙ ТРИВОГИ НА БІЛОЦЕРКІВЩИНІ
                    
                    Загрозу скасовано.
                    """;
        };
    }

    private String buildReason(AlertReason reason) {

        if (reason == null || reason.items().isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("Виявлена загроза:").append(System.lineSeparator());

        for (AlertReasonItem item : reason.items()) {

            sb.append("• ").append(item.category());
            sb.append(System.lineSeparator());
        }

        return sb.toString();
    }
}