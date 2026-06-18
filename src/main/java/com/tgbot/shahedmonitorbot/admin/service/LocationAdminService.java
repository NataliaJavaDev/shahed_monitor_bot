package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationAdminService {

    private final List<String> locations = new ArrayList<>();

    public LocationAdminService(AppProperties properties) {
        properties.monitor().locations().forEach(this::addLocation);
    }

    public List<String> getLocations() {
        return List.copyOf(locations);
    }

    public boolean addLocation(String location) {
        String normalized = TextNormalizer.normalize(location);

        if (normalized.isBlank() || locations.contains(normalized)) {
            return false;
        }

        locations.add(normalized);
        return true;
    }

    public boolean removeLocation(String location) {
        String normalized = TextNormalizer.normalize(location);
        return locations.remove(normalized);
    }
}