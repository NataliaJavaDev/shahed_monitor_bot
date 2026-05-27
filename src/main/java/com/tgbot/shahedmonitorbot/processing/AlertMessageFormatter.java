package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;

@Service
public class AlertMessageFormatter {

    public String format(String sourceName, String text) {
        return """
                ⚠️ Моніторинг
                
                Джерело: %s
                
                %s
                """.formatted(sourceName, text);
    }
}