package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TargetAdminService {

    private final Map<String, String> targets = new LinkedHashMap<>();

    public TargetAdminService(AppProperties properties) {
        properties.monitor().targets().forEach(target ->
                addTarget(target, target)
        );
    }

    public List<String> getTargets() {
        return List.copyOf(targets.keySet());
    }

    public List<String> getCategories() {
        return targets.values()
                .stream()
                .distinct()
                .toList();
    }

    public String getCategory(String target) {
        String normalized = TextNormalizer.normalize(target);
        return targets.getOrDefault(normalized, normalized);
    }

    public boolean addTarget(String target) {
        return addTarget(target, target);
    }

    public boolean addTarget(String target, String category) {
        String normalizedTarget = TextNormalizer.normalize(target);
        String normalizedCategory = TextNormalizer.normalize(category);

        if (
                normalizedTarget.isBlank()
                        || normalizedCategory.isBlank()
                        || targets.containsKey(normalizedTarget)
        ) {
            return false;
        }

        targets.put(normalizedTarget, normalizedCategory);
        return true;
    }

    public boolean removeTarget(String target) {
        String normalized = TextNormalizer.normalize(target);
        return targets.remove(normalized) != null;
    }
}