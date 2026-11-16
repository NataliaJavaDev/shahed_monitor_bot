package com.tgbot.shahedmonitorbot.manualalert;

import org.springframework.stereotype.Component;

@Component
public class ManualAlertMessageFormatter {

    public String format(AlertType type) {
        return switch (type) {

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

            case RED_TO_YELLOW -> """
                    \u2063
                    🔴 → 🟡Відбій червоної, зміна на жовтий рівень повітряної тривоги - БпЛА
                    """;

            case YELLOW_TO_RED -> """
                    \u2063
                    🟡 → 🔴Відбій жовтого, зміна на червоний рівень повітряної тривоги - 🚀 Ракетна загроза
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
}