package com.tgbot.shahedmonitorbot.manualalert;

import org.springframework.stereotype.Component;

@Component
public class ManualAlertMessageFormatter {

    public String format(AlertType type) {
        return switch (type) {

            case ALERT -> """
                    \u2063
                    🚨 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
                    Негайно пройдіть в укриття.
                    """;

            case YELLOW_ALERT -> """
                    \u2063
                    🟡 ЖОВТИЙ РІВЕНЬ НЕБЕЗПЕКИ НА БІЛОЦЕРКІВЩИНІ
                    
                    Будьте уважні та готові пройти в укриття.
                    """;

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
}