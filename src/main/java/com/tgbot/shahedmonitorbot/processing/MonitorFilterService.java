package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.admin.service.LocationAdminService;
import com.tgbot.shahedmonitorbot.admin.service.TargetAdminService;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MonitorFilterService {

    private final TargetAdminService targetAdminService;
    private final LocationAdminService locationAdminService;

    public MonitorFilterService(
            TargetAdminService targetAdminService,
            LocationAdminService locationAdminService
    ) {
        this.targetAdminService = targetAdminService;
        this.locationAdminService = locationAdminService;
    }

    public Optional<MonitorMatch> findMatch(String text) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }

        String normalizedText = TextNormalizer.normalize(text);

        String matchedTarget = findFirstMatch(
                normalizedText,
                targetAdminService.getTargets()
        );

        String matchedLocation = findFirstMatch(
                normalizedText,
                locationAdminService.getLocations()
        );

        if (matchedTarget != null && matchedLocation != null) {
            return Optional.of(new MonitorMatch(matchedTarget, matchedLocation));
        }

        if (matchedLocation != null && isOnlyLocation(normalizedText, matchedLocation)) {
            return Optional.of(new MonitorMatch(null, matchedLocation));
        }

        return Optional.empty();
    }

    private boolean isOnlyLocation(String normalizedText, String matchedLocation) {
        return normalizedText.equals(TextNormalizer.normalize(matchedLocation));
    }

    private String findFirstMatch(String normalizedText, List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        return values.stream()
                .filter(normalizedText::contains)
                .findFirst()
                .orElse(null);
    }
}