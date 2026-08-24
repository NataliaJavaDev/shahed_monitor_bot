package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryCategory;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryConfig;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryJsonService;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.admin.dictionary.DynamicConfig;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationAdminService {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;

    public LocationAdminService(
        DictionaryStorage storage,
        DictionaryJsonService jsonService
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
    }

    public synchronized List<String> getLocations() {

        return storage.get()
            .dictionaries()
            .locations()
            .stream()
            .flatMap(category -> category.aliases().stream())
            .toList();
    }

    public synchronized List<String> getCategories() {

        return storage.get()
            .dictionaries()
            .locations()
            .stream()
            .map(DictionaryCategory::category)
            .toList();
    }

    public synchronized String getCategory(String location) {

        String normalizedLocation = TextNormalizer.normalize(location);

        return storage.get()
            .dictionaries()
            .locations()
            .stream()
            .filter(category -> category.aliases()
                .stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedLocation::equals)
            )
            .map(DictionaryCategory::category)
            .findFirst()
            .orElse(normalizedLocation);
    }

    public synchronized boolean addLocation(String location) {
        return addLocation(location, location);
    }

    public synchronized boolean addLocation(String location, String category) {

        String normalizedLocation = TextNormalizer.normalize(location);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (normalizedLocation.isBlank() || normalizedCategory.isBlank()) {
            return false;
        }

        List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());

        boolean locationExists = categories.stream().anyMatch(item -> item.aliases()
            .stream()
            .map(TextNormalizer::normalize)
            .anyMatch(normalizedLocation::equals)
        );

        if (locationExists) {
            return false;
        }

        for (int index = 0; index < categories.size(); index++) {

            DictionaryCategory current = categories.get(index);

            if (!TextNormalizer.normalize(current.category()).equals(normalizedCategory)) {
                continue;
            }

            List<String> aliases = new ArrayList<>(current.aliases());

            aliases.add(normalizedLocation);
            categories.set(index, new DictionaryCategory(current.category(), current.displayName(), List.copyOf(aliases)));
            replaceLocations(categories);

            return true;
        }

        categories.add(new DictionaryCategory(normalizedCategory, null, List.of(normalizedLocation)));
        replaceLocations(categories);

        return true;
    }

    public synchronized boolean removeLocation(String location) {

        String normalizedLocation = TextNormalizer.normalize(location);

        List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());

        for (int index = 0; index < categories.size(); index++) {

            DictionaryCategory current = categories.get(index);
            List<String> aliases = new ArrayList<>(current.aliases());
            boolean removed = aliases.removeIf(alias -> TextNormalizer.normalize(alias).equals(normalizedLocation));

            if (!removed) {
                continue;
            }

            if (aliases.isEmpty()) {
                categories.remove(index);
            } else {
                categories.set(index, new DictionaryCategory(current.category(), current.displayName(), List.copyOf(aliases)));
            }

            replaceLocations(categories);

            return true;
        }

        return false;
    }

    private void replaceLocations(List<DictionaryCategory> locations) {

        DynamicConfig current = storage.get();
        DictionaryConfig currentDictionaries = current.dictionaries();

        DictionaryConfig updatedDictionaries = new DictionaryConfig(
            currentDictionaries.targets(),
            List.copyOf(locations),
            currentDictionaries.directions(),
            currentDictionaries.attention(),
            currentDictionaries.globalThreat(),
            currentDictionaries.forecast(),
            currentDictionaries.noise(),
            currentDictionaries.messageIntents()
        );

        storage.replace(new DynamicConfig(updatedDictionaries,current.sources()));
        jsonService.save();
    }
}