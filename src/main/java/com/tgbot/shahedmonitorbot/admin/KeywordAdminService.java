package com.tgbot.shahedmonitorbot.admin;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KeywordAdminService {

    private final List<String> keywords = new ArrayList<>();

    public KeywordAdminService(AppProperties properties) {
        properties.monitor().keywords().forEach(this::addKeyword);
    }

    public List<String> getKeywords() {
        return List.copyOf(keywords);
    }

    public boolean addKeyword(String keyword) {
        String normalized = TextNormalizer.normalize(keyword);

        if (normalized.isBlank() || keywords.contains(normalized)) {
            return false;
        }

        keywords.add(normalized);
        return true;
    }

    public boolean removeKeyword(String keyword) {
        String normalized = TextNormalizer.normalize(keyword);
        return keywords.remove(normalized);
    }
}