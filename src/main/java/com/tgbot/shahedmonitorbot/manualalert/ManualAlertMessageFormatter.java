package com.tgbot.shahedmonitorbot.manualalert;

import org.springframework.stereotype.Component;

// import com.tgbot.shahedmonitorbot.monitoring.reason.*;

@Component
public class ManualAlertMessageFormatter {

    public String format(AlertType type) {
        return switch (type) {

            case ALERT -> """
                    \u2063
                    🚨 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
                    Негайно пройдіть в укриття.
                    """;

            case RED_ALERT -> """
                    \u2063
                    🔴 🚀 Ракетна загроза!
                    📢Повітряна тривога на Білоцерківщині
                    """;

            case YELLOW_ALERT -> """
                    \u2063
                    🟡 БпЛА. 🛵 Дронова загроза!
                    📢Повітряна тривога на Білоцерківщині
                    """;

            case HIGH_RISK -> """
                    \u2063
                    🚨‼️ ПІДВИЩЕНА НЕБЕЗПЕКА НА БІЛОЦЕРКІВЩИНІ
                    
                    Негайно пройдіть в укриття.
                    """;

            case ALL_CLEAR -> """
                    \u2063
                    🟢 Відбій повітряної тривоги на Білоцерківщині
                    """;
        };
    }








    // public String format(AlertType type1, AlertReason reason) {
    //     return switch (type1) {

    //         case ALERT -> """
    //                 \u2063
    //                 🚨 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
    //                 %s
    //                 Негайно пройдіть в укриття.
    //                 """
    //                 .formatted(buildReason(reason));

    //             case RED_ALERT -> """
    //                 \u2063
    //                 🔴 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
    //                 %s
    //                 Негайно пройдіть в укриття.
    //                 """
    //                 .formatted(buildReason(reason));

    //             case YELLOW_ALERT -> """
    //                 \u2063
    //                 🟡 ПОВІТРЯНА ТРИВОГА НА БІЛОЦЕРКІВЩИНІ
                    
    //                 %s
    //                 Негайно пройдіть в укриття.
    //                 """
    //                 .formatted(buildReason(reason));

    //         case HIGH_RISK -> """
    //                 \u2063
    //                 ‼️ ПІДВИЩЕНА НЕБЕЗПЕКА НА БІЛОЦЕРКІВЩИНІ
                    
    //                 Негайно пройдіть в укриття.
    //                 """;

    //         case ALL_CLEAR -> """
    //                 \u2063
    //                 ✅ ВІДБІЙ ТРИВОГИ НА БІЛОЦЕРКІВЩИНІ
                    
    //                 Загрозу скасовано.
    //                 """;
    //     };
    // }
}