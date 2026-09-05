package com.tgbot.shahedmonitorbot.admin.dictionary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class DictionaryJsonService {

    private final ObjectMapper objectMapper;
    private final DictionaryStorage storage;
    private final AppProperties properties;

    public DictionaryJsonService(
        DictionaryStorage storage,
        AppProperties properties
    ) {
        this.storage = storage;
        this.properties = properties;
        this.objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
    }

    public void save() {

        try {

            Path path = Path.of(properties.dictionary().file());
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writeValue(path.toFile(), storage.get());

        } catch (IOException exception) {

            throw new IllegalStateException("Failed to save dictionaries to JSON", exception);
        }
    }

    public DynamicConfig load() {

        try {

            Path path = Path.of(properties.dictionary().file());

            if (!Files.exists(path)) {
                return null;
            }

            return objectMapper.readValue(path.toFile(), DynamicConfig.class);

        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load dictionaries from JSON", exception);
        }
    }
}