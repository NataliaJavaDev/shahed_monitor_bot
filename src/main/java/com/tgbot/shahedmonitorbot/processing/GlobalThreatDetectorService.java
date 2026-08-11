package com.tgbot.shahedmonitorbot.processing;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class GlobalThreatDetectorService {

    private static final List<String> GLOBAL_THREAT_MARKERS = List.of(
            "загроза балістики",
            "балістика",
            "балістична",
            "балістичні",
            "міг-31к",
            "міг",
            "ту-95",
            "ту95",
            "ту-160",
            "ту160",
            "калібри",
            "пуск калібрів",
            "пуски калібрів",
            "крилаті",
            "пуск крилатих",
            "пуски крилатих",
            "пустили крилаті",
            "циркон",
            "циркони",
            "кинджал",
            "кинджали",
            "ракетоносії",
            "вихід ракетоносіїв",
            "бастіон",
            "розгортання бастіон"
    );

    private final MarkerDetectorService markerDetectorService;

    public GlobalThreatDetectorService(
            MarkerDetectorService markerDetectorService
    ) {
        this.markerDetectorService = markerDetectorService;
    }

    public Optional<String> findGlobalThreat(String text) {
        return markerDetectorService.findMatchedMarker(
                text,
                GLOBAL_THREAT_MARKERS
        );
    }
}