package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GlobalThreatDetectorService {

    private final MarkerDetectorService markerDetectorService;
    private final DictionaryStorage storage;

    public GlobalThreatDetectorService(
        MarkerDetectorService markerDetectorService,
        DictionaryStorage storage
    ) {
        this.markerDetectorService = markerDetectorService;
        this.storage = storage;
    }

    public Optional<GlobalThreatMatch> findGlobalThreat(String text) {
        return markerDetectorService.findMatchedMarker(text, storage.get().dictionaries().globalThreat()).map(GlobalThreatMatch::new);
    }
}