package com.tgbot.shahedmonitorbot.monitoring.source;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryJsonService;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.admin.dictionary.DynamicConfig;
import com.tgbot.shahedmonitorbot.admin.enums.SourceStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitoredSourceService {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;

    public MonitoredSourceService(
        DictionaryStorage storage,
        DictionaryJsonService jsonService
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
    }

    public synchronized List<MonitoredSource> getAllSources() {

        return List.copyOf(storage.get().sources());
    }

    public synchronized List<MonitoredSource> getActiveSources() {

        return storage.get()
            .sources()
            .stream()
            .filter(source -> source.status() == SourceStatus.ACTIVE)
            .toList();
    }

    public synchronized List<MonitoredSource> getIgnoredSources() {

        return storage.get()
            .sources()
            .stream()
            .filter(source -> source.status() == SourceStatus.IGNORED)
            .toList();
    }

    public synchronized MonitoredSource findByChatId(String chatId) {

        if (chatId == null || chatId.isBlank()) {
            return null;
        }

        return storage.get()
            .sources()
            .stream()
            .filter(source -> source.chatId().equals(chatId))
            .findFirst()
            .orElse(null);
    }

    public synchronized boolean isKnown(String chatId) {
        return findByChatId(chatId) != null;
    }

    public synchronized boolean isMonitored(String chatId) {

        MonitoredSource source = findByChatId(chatId);
        return source != null && source.status() == SourceStatus.ACTIVE;
    }

    public synchronized boolean addSource(String chatId, String title, SourceStatus status) {

        if (chatId == null || chatId.isBlank() || status == null) {
            return false;
        }

        if (findByChatId(chatId) != null) {
            return false;
        }

        List<MonitoredSource> sources = new java.util.ArrayList<>(storage.get().sources());

        sources.add(new MonitoredSource(chatId, safeTitle(title), status));
        replaceSources(sources);

        return true;
    }

    public synchronized boolean addActiveSource(String chatId, String title) {
        return addSource(chatId, title, SourceStatus.ACTIVE);
    }

    public synchronized boolean addIgnoredSource(String chatId, String title) {
        return addSource(chatId, title, SourceStatus.IGNORED);
    }

    public synchronized boolean enableSource(String chatId) {
        return changeStatus(chatId, SourceStatus.ACTIVE);
    }

    public synchronized boolean ignoreSource(String chatId) {
        return changeStatus(chatId, SourceStatus.IGNORED);
    }

    private boolean changeStatus(String chatId, SourceStatus status) {

        List<MonitoredSource> sources = new java.util.ArrayList<>(storage.get().sources());

        for (int index = 0; index < sources.size(); index++) {

            MonitoredSource current = sources.get(index);

            if (!current.chatId().equals(chatId)) {
                continue;
            }

            if (current.status() == status) {
                return false;
            }

            sources.set(index, new MonitoredSource(current.chatId(), current.title(), status));
            replaceSources(sources);
            
            return true;
        }

        return false;
    }

    private void replaceSources(List<MonitoredSource> sources) {

        DynamicConfig current = storage.get();

        storage.replace(new DynamicConfig(current.dictionaries(), List.copyOf(sources)));
        jsonService.save();
    }

    private String safeTitle(String title) {

        if (title == null || title.isBlank()) {
            return "Невідоме джерело";
        }

        return title.trim();
    }
}