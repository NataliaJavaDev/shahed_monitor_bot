package com.tgbot.shahedmonitorbot.monitoring.history;

import com.tgbot.shahedmonitorbot.processing.MessageAnalysis;

import java.util.ArrayList;
import java.util.List;

public class HistoricalThreatState {

    private MessageAnalysis forecast;
    private MessageAnalysis currentThreat;
    private MessageAnalysis latestRoute;
    private MessageAnalysis latestCount;
    private MessageAnalysis latestAttention;
    private int totalMessages;
    private int analyzedMessages;

    private final List<MessageAnalysis> timeline = new ArrayList<>();
    private final List<MessageAnalysis> forecasts = new ArrayList<>();

    public MessageAnalysis getForecast() {
        return forecast;
    }

    public void setForecast(MessageAnalysis forecast) {
        this.forecast = forecast;
    }

    public MessageAnalysis getCurrentThreat() {
        return currentThreat;
    }

    public void setCurrentThreat(MessageAnalysis currentThreat) {
        this.currentThreat = currentThreat;
    }

    public MessageAnalysis getLatestRoute() {
        return latestRoute;
    }

    public void setLatestRoute(MessageAnalysis latestRoute) {
        this.latestRoute = latestRoute;
    }

    public MessageAnalysis getLatestCount() {
        return latestCount;
    }

    public void setLatestCount(MessageAnalysis latestCount) {
        this.latestCount = latestCount;
    }

    public MessageAnalysis getLatestAttention() {
        return latestAttention;
    }

    public void setLatestAttention(MessageAnalysis latestAttention) {
        this.latestAttention = latestAttention;
    }

    public List<MessageAnalysis> getTimeline() {
        return timeline;
    }

    public int getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(int totalMessages) {
        this.totalMessages = totalMessages;
    }

    public int getAnalyzedMessages() {
        return analyzedMessages;
    }

    public void setAnalyzedMessages(int analyzedMessages) {
        this.analyzedMessages = analyzedMessages;
    }
}