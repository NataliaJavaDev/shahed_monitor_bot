package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.config.AppProperties;

import java.util.Optional;

@Service
public class GlobalThreatDetectorService {

    private final MarkerDetectorService markerDetectorService;
    private final AppProperties appProperties;

    public GlobalThreatDetectorService(
        MarkerDetectorService markerDetectorService,
        AppProperties appProperties
    ) {
        this.markerDetectorService = markerDetectorService;
        this.appProperties = appProperties;
    }

    public Optional<GlobalThreatMatch> findGlobalThreat(String text) {
        return markerDetectorService.findMatchedMarker(
            text,
            appProperties.monitor().globalThreatMarkers()
        ).map(GlobalThreatMatch::new);
    }
}