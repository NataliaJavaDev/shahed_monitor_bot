package com.tgbot.shahedmonitorbot.tdlib;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSourceService;
import org.springframework.stereotype.Service;

@Service
public class TdLibUpdateHandler {

    private final MonitoredSourceService monitoredSourceService;

    public TdLibUpdateHandler(MonitoredSourceService monitoredSourceService) {
        this.monitoredSourceService = monitoredSourceService;
    }

    public void handle(String update) {

        if (!update.contains("\"@type\":\"updateNewMessage\"")) {
            return;
        }

        String chatId = extractChatId(update);

        if (chatId == null) {
            System.out.println("NEW MESSAGE without chat_id:");
            System.out.println(update);
            return;
        }

        if (!monitoredSourceService.isMonitored(chatId)) {
            System.out.println("Ignored message from chat: " + chatId);
            return;
        }

        System.out.println("NEW MESSAGE FROM MONITORED SOURCE:");
        System.out.println(update);
    }

    private String extractChatId(String update) {
        String marker = "\"chat_id\":";
        int start = update.indexOf(marker);

        if (start == -1) {
            return null;
        }

        start += marker.length();

        int end = update.indexOf(",", start);

        if (end == -1) {
            end = update.indexOf("}", start);
        }

        if (end == -1) {
            return null;
        }

        return update.substring(start, end).trim();
    }
}