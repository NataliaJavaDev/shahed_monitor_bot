package com.tgbot.shahedmonitorbot.admin.dictionary;

import com.tgbot.shahedmonitorbot.admin.enums.SourceStatus;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryInitializer {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;
    private final AppProperties properties;

    public DictionaryInitializer(
        DictionaryStorage storage,
        DictionaryJsonService jsonService,
        AppProperties properties
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
        this.properties = properties;
    }

    @PostConstruct
    public void initialize() {

        DynamicConfig config = jsonService.load();

        if (config != null) {
            storage.replace(config);
            return;
        }

        DynamicConfig initialConfig = createFromProperties();

        storage.replace(initialConfig);
        jsonService.save();
    }

    private DynamicConfig createFromProperties() {

        AppProperties.Monitor monitor = properties.monitor();

        DictionaryConfig dictionaries = new DictionaryConfig(
            monitor.targetCategories().stream()
                .map(category -> new DictionaryCategory(
                    category.category(),
                    category.displayName(),
                    category.aliases()))
                .toList(),

            monitor.locationCategories().stream()
                .map(category -> new DictionaryCategory(
                    category.category(),
                    null,
                    category.aliases()))
                .toList(),

            List.copyOf(monitor.directions()),

            monitor.attentionWords().stream()
                .map(category -> new DictionaryCategory(
                    category.category(),
                    null,
                    category.aliases()))
                .toList(),

            List.copyOf(monitor.globalThreatMarkers()),
            List.copyOf(monitor.forecastMarkers()),
            List.copyOf(monitor.noiseMarkers()),

            monitor.messageIntents().stream()
                .map(intent -> new DictionaryIntent(intent.intent(), intent.aliases()))
                .toList()
        );

        List<MonitoredSource> sources = monitor.sources() == null ? List.of() : monitor.sources()
            .stream()
            .map(source -> new MonitoredSource(
                source.chatId(),
                source.title(),
                Boolean.TRUE.equals(source.active()) ? SourceStatus.ACTIVE : SourceStatus.IGNORED))
            .toList();

        return new DynamicConfig(
            dictionaries,
            sources
        );
    }
}