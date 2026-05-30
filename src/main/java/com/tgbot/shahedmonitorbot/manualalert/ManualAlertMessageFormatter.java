package com.tgbot.shahedmonitorbot.manualalert;

import org.springframework.stereotype.Component;

@Component
public class ManualAlertMessageFormatter {

    public String format(ManualAlertType type) {
        return switch (type) {
            case ALERT -> """
                    🚨 ПОВІТРЯНА ТРИВОГА
                    
                    Негайно пройдіть в укриття.
                    """;

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
}