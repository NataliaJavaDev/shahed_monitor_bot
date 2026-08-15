package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectionAdminService {

    private final List<String> directions = new ArrayList<>();

    public DirectionAdminService(AppProperties properties) {
        properties.monitor().directions().forEach(this::addDirection);
    }

    public List<String> getDirections() {
        return List.copyOf(directions);
    }

    public boolean addDirection(String direction) {

        String normalized = TextNormalizer.normalize(direction);

        if (normalized.isBlank() || directions.contains(normalized)) {
            return false;
        }

        directions.add(normalized);
        return true;
    }

    public boolean removeDirection(String direction) {
        
        String normalized = TextNormalizer.normalize(direction);
        return directions.remove(normalized);
    }
}