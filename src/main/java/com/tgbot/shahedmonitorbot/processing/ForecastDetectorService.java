package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;
import com.tgbot.shahedmonitorbot.config.AppProperties;

import java.util.Optional;

@Service
public class ForecastDetectorService {

    // private static final List<String> FORECAST_MARKERS = List.of(
    //         "прогноз",
    //         "маленький прогноз",
    //         "очікується",
    //         "можливий",
    //         "можливі",
    //         "можлива",
    //         "можливе",
    //         "майте на увазі",
    //         "зверніть увагу",
    //         "попередньо",
    //         "приблизно по",
    //         "станом на зараз",
    //         "на цю ніч",
    //         "цієї ночі",
    //         "сьогодні",
    //         "найближчим часом",
    //         "найближчими годинами",
    //         "можуть бути тривоги",
    //         "можливі тривоги",
    //         "масштабна тривога",
    //         "оголошення масштабної тривоги",
    //         "в повітрі",
    //         "борти",
    //         "по тушках"
    // );

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