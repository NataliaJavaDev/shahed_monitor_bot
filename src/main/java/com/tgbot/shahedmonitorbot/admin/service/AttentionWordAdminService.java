package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AttentionWordAdminService {

    private final Map<String, String> aliasToCategory = new LinkedHashMap<>();
    private final Map<String, List<String>> categoryToAliases = new LinkedHashMap<>();

    public AttentionWordAdminService(AppProperties properties) {
        properties.monitor().attentionWords().forEach(category -> {
            String categoryName = category.category();

            if (category.aliases() == null || category.aliases().isEmpty()) {
                addAttentionWord(categoryName, categoryName);
                return;
            }

            category.aliases().forEach(alias ->
                    addAttentionWord(alias, categoryName)
            );
        });
    }

    public List<String> getAttentionWords() {
        return List.copyOf(aliasToCategory.keySet());
    }

    public List<String> getCategories() {
        return List.copyOf(categoryToAliases.keySet());
    }

    public String getCategory(String attentionWord) {
        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);
        return aliasToCategory.getOrDefault(normalizedAttentionWord, normalizedAttentionWord);
    }

    public boolean containsAttentionWord(String text) {
        String normalizedText = TextNormalizer.normalize(text);

        return aliasToCategory.keySet().stream()
                .anyMatch(normalizedText::contains);
    }

    public String findAttentionWord(String text) {
        String normalizedText = TextNormalizer.normalize(text);

        return aliasToCategory.keySet().stream()
                .filter(normalizedText::contains)
                .findFirst()
                .orElse(null);
    }

    public boolean addAttentionWord(String attentionWord) {
        return addAttentionWord(attentionWord, attentionWord);
    }

    public boolean addAttentionWord(String attentionWord, String category) {
        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (
                normalizedAttentionWord.isBlank()
                        || normalizedCategory.isBlank()
                        || aliasToCategory.containsKey(normalizedAttentionWord)
        ) {
            return false;
        }

        aliasToCategory.put(normalizedAttentionWord, normalizedCategory);

        categoryToAliases
                .computeIfAbsent(normalizedCategory, key -> new ArrayList<>())
                .add(normalizedAttentionWord);

        return true;
    }

    public boolean removeAttentionWord(String attentionWord) {
        String normalizedAttentionWord = TextNormalizer.normalize(attentionWord);

        String category = aliasToCategory.remove(normalizedAttentionWord);

        if (category == null) {
            return false;
        }

        List<String> aliases = categoryToAliases.get(category);

        if (aliases != null) {
            aliases.remove(normalizedAttentionWord);

            if (aliases.isEmpty()) {
                categoryToAliases.remove(category);
            }
        }

        return true;
    }
}