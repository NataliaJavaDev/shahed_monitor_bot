package com.tgbot.shahedmonitorbot.deduplication;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.processing.ForecastMatch;
import com.tgbot.shahedmonitorbot.processing.GlobalThreatMatch;
import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import com.tgbot.shahedmonitorbot.processing.ThreatMatch;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class DeduplicationService {

    private final AppProperties appProperties;
    private final Map<String, Instant> seenEvents = new ConcurrentHashMap<>();

    public DeduplicationService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public boolean isDuplicate(MonitorMatch match) {

        String key = buildDeduplicationKey(match);

        if (key.isBlank()) {
            return true;
        }

        cleanupExpired();

        if (seenEvents.containsKey(key)) {
            return true;
        }

        seenEvents.put(key, Instant.now());
        return false;
    }

    public boolean isDuplicate(ThreatMatch threatMatch) {

        String key = buildThreatDeduplicationKey(threatMatch);

        if (key.isBlank()) {
            return true;
        }

        cleanupExpired();

        if (seenEvents.containsKey(key)) {
            return true;
        }

        seenEvents.put(key, Instant.now());
        return false;
    }

    public boolean isDuplicate(GlobalThreatMatch globalThreatMatch) {

        String key = buildGlobalThreatDeduplicationKey(globalThreatMatch);

        if (key.isBlank()) {
            return true;
        }

        cleanupExpired();

        if (seenEvents.containsKey(key)) {
            return true;
        }

        seenEvents.put(key, Instant.now());
        return false;
    }

    public boolean isDuplicate(ForecastMatch forecastMatch) {

        String key = buildForecastDeduplicationKey(forecastMatch);

        if (key.isBlank()) {
            return true;
        }

        cleanupExpired();

        if (seenEvents.containsKey(key)) {
            return true;
        }

        seenEvents.put(key, Instant.now());
        return false;
    }

    public String buildDeduplicationKey(MonitorMatch match) {

        String targetCategory = normalizeOrFallback(match.targetCategory(), "NO_TARGET");
        String location = normalizeOrFallback(match.locationCategory(), "NO_LOCATION");

        return targetCategory + "::" + location;
    }

    public String buildThreatDeduplicationKey(ThreatMatch threatMatch) {

        String threatCategory =
            normalizeOrFallback(
                threatMatch.threatCategory(),
                "NO_THREAT_CATEGORY"
            );

        String matchedThreat =
            normalizeOrFallback(
                threatMatch.matchedThreat(),
                "NO_THREAT"
            );

        return "THREAT::" + threatCategory + "::" + matchedThreat;
    }

    public String buildGlobalThreatDeduplicationKey(GlobalThreatMatch globalThreatMatch) {

        String matchedMarker =
            normalizeOrFallback(
                globalThreatMatch.matchedMarker(),
                "NO_GLOBAL_THREAT"
            );

        return "GLOBAL_THREAT::" + matchedMarker;
    }

    public String buildForecastDeduplicationKey(ForecastMatch forecastMatch) {

        String matchedMarker =
            normalizeOrFallback(
                forecastMatch.matchedMarker(),
                "NO_FORECAST"
            );

        return "FORECAST::" + matchedMarker;
    }

    private String normalizeOrFallback(String value, String fallback) {

        if (value == null || value.isBlank()) {
            return fallback;
        }

        return TextNormalizer.normalize(value);
    }

    private void cleanupExpired() {

        int ttlMinutes = appProperties.monitor().duplicateTtlMinutes();
        Instant threshold = Instant.now().minus(Duration.ofMinutes(ttlMinutes));

        seenEvents.entrySet().removeIf(entry -> entry.getValue().isBefore(threshold));
    }
}