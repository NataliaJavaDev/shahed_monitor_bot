package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TargetAdminService {

    private final Map<String, String> aliasToCategory = new LinkedHashMap<>();
    private final Map<String, List<String>> categoryToAliases = new LinkedHashMap<>();
    private final Map<String, String> categoryToDisplayName = new LinkedHashMap<>();

    public TargetAdminService(AppProperties properties) {

        properties.monitor().targetCategories().forEach(category -> {
            String categoryName = category.category();

            categoryToDisplayName.put(
                categoryName,
                category.displayName() != null
                    ? category.displayName()
                    : categoryName
            );

            if (category.aliases() == null || category.aliases().isEmpty()) {
                addTarget(categoryName, categoryName);
                return;
            }

            category.aliases().forEach(alias ->
                addTarget(alias, categoryName)
            );
        });
    }

    public List<String> getTargets() {
        return List.copyOf(aliasToCategory.keySet());
    }

    public List<String> getCategories() {
        return List.copyOf(categoryToAliases.keySet());
    }

    public List<String> getAliasesByCategory(String category) {

        String normalizedCategory = TextNormalizer.normalize(category);
        return List.copyOf(categoryToAliases.getOrDefault(normalizedCategory, List.of()));
    }

    public String getCategory(String target) {

        String normalizedTarget = TextNormalizer.normalize(target);
        return aliasToCategory.getOrDefault(normalizedTarget, normalizedTarget);
    }

    public boolean addTarget(String target) {
        return addTarget(target, target);
    }

    public boolean addTarget(String target, String category) {

        String normalizedTarget = TextNormalizer.normalize(target);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (normalizedTarget.isBlank() || normalizedCategory.isBlank() || aliasToCategory.containsKey(normalizedTarget)) {
            return false;
        }

        aliasToCategory.put(normalizedTarget, normalizedCategory);
        categoryToAliases.computeIfAbsent(normalizedCategory, key -> new ArrayList<>()).add(normalizedTarget);

        return true;
    }

    public boolean removeTarget(String target) {

        String normalizedTarget = TextNormalizer.normalize(target);
        String category = aliasToCategory.remove(normalizedTarget);

        if (category == null) {
            return false;
        }

        List<String> aliases = categoryToAliases.get(category);

        if (aliases != null) {
            aliases.remove(normalizedTarget);

            if (aliases.isEmpty()) {
                categoryToAliases.remove(category);
            }
        }

        return true;
    }

    public String getDisplayName(String category) {

        String normalizedCategory = TextNormalizer.normalize(category);
        return categoryToDisplayName.getOrDefault(normalizedCategory, normalizedCategory);
    }
}