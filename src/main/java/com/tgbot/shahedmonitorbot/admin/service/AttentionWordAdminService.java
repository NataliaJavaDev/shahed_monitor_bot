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
public class AttentionWordAdminService {

    private final DictionaryStorage storage;
    private final DictionaryJsonService jsonService;

    public AttentionWordAdminService(
        DictionaryStorage storage,
        DictionaryJsonService jsonService
    ) {
        this.storage = storage;
        this.jsonService = jsonService;
    }

    public synchronized List<String> getAttentionWords() {

        return storage.get()
            .dictionaries()
            .attention()
            .stream()
            .flatMap(category -> category.aliases().stream())
            .toList();
    }

    public synchronized List<String> getCategories() {

        return storage.get()
            .dictionaries()
            .attention()
            .stream()
            .map(DictionaryCategory::category)
            .toList();
    }

    public synchronized String getCategory(String attentionWord) {

        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);

        return storage.get()
            .dictionaries()
            .attention()
            .stream()
            .filter(category -> category.aliases()
                .stream()
                .map(TextNormalizer::normalize)
                .anyMatch(normalizedAttentionWord::equals)
            )
            .map(DictionaryCategory::category)
            .findFirst()
            .orElse(normalizedAttentionWord);
    }

    public synchronized boolean containsAttentionWord(String text) {

        String normalizedText = TextNormalizer.normalize(text);

        return getAttentionWords()
            .stream()
            .map(TextNormalizer::normalize)
            .anyMatch(normalizedText::contains);
    }

    public synchronized String findAttentionWord(String text) {

        String normalizedText = TextNormalizer.normalize(text);

        return getAttentionWords()
            .stream()
            .filter(normalizedText::contains)
            .findFirst()
            .orElse(null);
    }

    public synchronized boolean addAttentionWord(String attentionWord) {
        return addAttentionWord(attentionWord, attentionWord);
    }

    public synchronized boolean addAttentionWord(String attentionWord, String category) {

        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (normalizedAttentionWord.isBlank() || normalizedCategory.isBlank()) {
            return false;
        }

        List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().attention());

        boolean wordExists = categories.stream().anyMatch(item -> item.aliases()
            .stream()
            .map(TextNormalizer::normalize)
            .anyMatch(normalizedAttentionWord::equals)
        );

        if (wordExists) {
            return false;
        }

        for (int index = 0; index < categories.size(); index++) {

            DictionaryCategory current = categories.get(index);

            if (!TextNormalizer.normalize(current.category()).equals(normalizedCategory)) {
                continue;
            }

            List<String> aliases = new ArrayList<>(current.aliases());

            aliases.add(normalizedAttentionWord);
            categories.set(index, new DictionaryCategory(current.category(), current.displayName(), List.copyOf(aliases)));
            replaceAttention(categories);

            return true;
        }

        categories.add(new DictionaryCategory(normalizedCategory, null, List.of(normalizedAttentionWord)));
        replaceAttention(categories);

        return true;
    }

    public synchronized boolean removeAttentionWord(String attentionWord) {

        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);
        List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().attention());

        for (int index = 0; index < categories.size(); index++) {

            DictionaryCategory current = categories.get(index);
            List<String> aliases = new ArrayList<>(current.aliases());
            boolean removed = aliases.removeIf(alias -> TextNormalizer.normalize(alias).equals(normalizedAttentionWord));

            if (!removed) {
                continue;
            }

            if (aliases.isEmpty()) {
                categories.remove(index);
            } else {
                categories.set(index,new DictionaryCategory(current.category(), current.displayName(), List.copyOf(aliases)));
            }

            replaceAttention(categories);

            return true;
        }

        return false;
    }

    private void replaceAttention(List<DictionaryCategory> attention) {

        DynamicConfig current = storage.get();
        DictionaryConfig currentDictionaries = current.dictionaries();

        DictionaryConfig updatedDictionaries = new DictionaryConfig(
            currentDictionaries.targets(),
            currentDictionaries.locations(),
            currentDictionaries.directions(),
            List.copyOf(attention),
            currentDictionaries.globalThreat(),
            currentDictionaries.forecast(),
            currentDictionaries.noise(),
            currentDictionaries.messageIntents()
        );

        storage.replace(new DynamicConfig(updatedDictionaries, current.sources()));
        jsonService.save();
    }
}