package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TargetAdminService {

    private final List<String> targets = new ArrayList<>();

    public TargetAdminService(AppProperties properties) {
        properties.monitor().targets().forEach(this::addTarget);
    }

    public List<String> getTargets() {
        return List.copyOf(targets);
    }

    public boolean addTarget(String target) {
        String normalized = TextNormalizer.normalize(target);

        if (normalized.isBlank() || targets.contains(normalized)) {
            return false;
        }

        targets.add(normalized);
        return true;
    }

    public boolean removeTarget(String target) {
        String normalized = TextNormalizer.normalize(target);
        return targets.remove(normalized);
    }
}