package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class LocationAdminService {

    private final Map<String, String> aliasToCategory = new LinkedHashMap<>();
    private final Map<String, List<String>> categoryToAliases = new LinkedHashMap<>();

    public LocationAdminService(AppProperties properties) {
        properties.monitor().locationCategories().forEach(category -> {
            String categoryName = category.category();

            if (category.aliases() == null || category.aliases().isEmpty()) {
                addLocation(categoryName, categoryName);
                return;
            }

            category.aliases().forEach(alias ->
                    addLocation(alias, categoryName)
            );
        });
    }

    public List<String> getLocations() {
        return List.copyOf(aliasToCategory.keySet());
    }

    public List<String> getCategories() {
        return List.copyOf(categoryToAliases.keySet());
    }

    public String getCategory(String location) {
        String normalizedLocation = TextNormalizer.normalize(location);
        return aliasToCategory.getOrDefault(normalizedLocation, normalizedLocation);
    }

    public boolean addLocation(String location) {
        return addLocation(location, location);
    }

    public boolean addLocation(String location, String category) {
        String normalizedLocation = TextNormalizer.normalize(location);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (
                normalizedLocation.isBlank()
                        || normalizedCategory.isBlank()
                        || aliasToCategory.containsKey(normalizedLocation)
        ) {
            return false;
        }

        aliasToCategory.put(normalizedLocation, normalizedCategory);

        categoryToAliases
                .computeIfAbsent(normalizedCategory, key -> new ArrayList<>())
                .add(normalizedLocation);

        return true;
    }

    public boolean removeLocation(String location) {
        String normalizedLocation = TextNormalizer.normalize(location);

        String category = aliasToCategory.remove(normalizedLocation);

        if (category == null) {
            return false;
        }

        List<String> aliases = categoryToAliases.get(category);

        if (aliases != null) {
            aliases.remove(normalizedLocation);

            if (aliases.isEmpty()) {
                categoryToAliases.remove(category);
            }
        }

        return true;
    }
}