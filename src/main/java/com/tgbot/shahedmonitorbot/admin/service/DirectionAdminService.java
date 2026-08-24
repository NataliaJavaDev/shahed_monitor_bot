package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryConfig;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryJsonService;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.admin.dictionary.DynamicConfig;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectionAdminService {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;

    public DirectionAdminService(
        DictionaryStorage storage,
        DictionaryJsonService jsonService
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
    }

    public synchronized List<String> getDirections() {
        return List.copyOf(storage.get().dictionaries().directions());
    }

    public synchronized boolean addDirection(String direction) {

        String normalized = TextNormalizer.normalize(direction);

        if (normalized.isBlank()) {
            return false;
        }

        List<String> directions = new ArrayList<>(storage.get().dictionaries().directions());

        if (directions.contains(normalized)) {
            return false;
        }

        directions.add(normalized);
        replaceDirections(directions);

        return true;
    }

    public synchronized boolean removeDirection(String direction) {

        String normalized = TextNormalizer.normalize(direction);
        List<String> directions = new ArrayList<>(storage.get().dictionaries().directions());

        if (!directions.remove(normalized)) {
            return false;
        }

        replaceDirections(directions);

        return true;
    }

    private void replaceDirections(List<String> directions) {

        DynamicConfig current = storage.get();
        DictionaryConfig currentDictionaries = current.dictionaries();

        DictionaryConfig updatedDictionaries = new DictionaryConfig(
            currentDictionaries.targets(),
            currentDictionaries.locations(),
            List.copyOf(directions),
            currentDictionaries.attention(),
            currentDictionaries.globalThreat(),
            currentDictionaries.forecast(),
            currentDictionaries.noise(),
            currentDictionaries.messageIntents()
        );

        storage.replace(new DynamicConfig(updatedDictionaries, current.sources()));
        jsonService.save();
    }
}