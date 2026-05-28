package com.tgbot.shahedmonitorbot.filter;

import com.tgbot.shahedmonitorbot.admin.KeywordAdminService;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

@Service
public class KeywordMatcherService {

    private final KeywordAdminService keywordAdminService;

    public KeywordMatcherService(KeywordAdminService keywordAdminService) {
        this.keywordAdminService = keywordAdminService;
    }

    public boolean isRelevant(String text) {
        String normalizedText = TextNormalizer.normalize(text);

        return keywordAdminService.getKeywords().stream()
                .anyMatch(normalizedText::contains);
    }
}