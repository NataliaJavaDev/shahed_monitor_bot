package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ForecastDetectorService {

    private final MarkerDetectorService markerDetectorService;
    private final DictionaryStorage storage;

    public ForecastDetectorService(
        MarkerDetectorService markerDetectorService,
        DictionaryStorage storage
    ) {
        this.markerDetectorService = markerDetectorService;
        this.storage = storage;
    }

    public Optional<ForecastMatch> findForecast(String text) {
        return markerDetectorService.findMatchedMarker(text, storage.get().dictionaries().forecast()).map(ForecastMatch::new);
    }
}