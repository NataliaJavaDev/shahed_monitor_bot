package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ForecastDetectorService {

    private static final List<String> FORECAST_MARKERS = List.of(
            "прогноз",
            "маленький прогноз",
            "очікується",
            "можливий",
            "можливі",
            "можлива",
            "можливе",
            "майте на увазі",
            "зверніть увагу",
            "попередньо",
            "приблизно по",
            "станом на зараз",
            "на цю ніч",
            "цієї ночі",
            "сьогодні",
            "найближчим часом",
            "найближчими годинами",
            "можуть бути тривоги",
            "можливі тривоги",
            "масштабна тривога",
            "оголошення масштабної тривоги",
            "в повітрі",
            "борти",
            "по тушках"
    );

    private final MarkerDetectorService markerDetectorService;

    public ForecastDetectorService(
            MarkerDetectorService markerDetectorService
    ) {
        this.markerDetectorService = markerDetectorService;
    }

    public Optional<String> findForecast(String text) {
        return markerDetectorService.findMatchedMarker(
                text,
                FORECAST_MARKERS
        );
    }
}