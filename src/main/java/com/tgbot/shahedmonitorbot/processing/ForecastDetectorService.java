package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.config.AppProperties;

import java.util.Optional;

@Service
public class ForecastDetectorService {

    private final MarkerDetectorService markerDetectorService;
    private final AppProperties appProperties;

    public ForecastDetectorService(
            MarkerDetectorService markerDetectorService,
            AppProperties appProperties
    ) {
        this.markerDetectorService = markerDetectorService;
        this.appProperties = appProperties;
    }

    public Optional<ForecastMatch> findForecast(String text) {
        return markerDetectorService
                .findMatchedMarker(
                        text,
                        appProperties.monitor().forecastMarkers()
                )
                .map(ForecastMatch::new);
    }
}