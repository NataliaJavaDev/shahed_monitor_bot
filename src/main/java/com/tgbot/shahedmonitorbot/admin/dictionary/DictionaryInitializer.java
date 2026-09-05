package com.tgbot.shahedmonitorbot.admin.dictionary;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class DictionaryInitializer {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;

    public DictionaryInitializer(
        DictionaryStorage storage,
        DictionaryJsonService jsonService
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
    }

    @PostConstruct
    public void initialize() {

        DynamicConfig config = jsonService.load();

        if (config == null) {
            throw new IllegalStateException("Dynamic configuration file was not found");
        }

        storage.replace(config);
    }
}