package com.tgbot.shahedmonitorbot.processing;

import com.tgbot.shahedmonitorbot.context.EventContextService;
import org.springframework.stereotype.Service;

@Service
public class ContextResolverService {

    private final EventContextService eventContextService;

    public ContextResolverService(EventContextService eventContextService) {
        this.eventContextService = eventContextService;
    }

    public ContextResolution resolve(MonitorMatch initialMatch) {
        if (initialMatch.matchType() != MatchType.LOCATION_ONLY) {
            return new ContextResolution(initialMatch, false);
        }

        return eventContextService.getLastEvent()
                .filter(previous -> previous.targetCategory() != null)
                .filter(previous -> !previous.targetCategory().isBlank())
                .map(previous -> new ContextResolution(
                        new MonitorMatch(
                                previous.matchedTarget(),
                                previous.targetCategory(),
                                initialMatch.direction(),
                                initialMatch.matchedLocation(),
                                initialMatch.locationCategory(),
                                MatchType.TARGET_AND_LOCATION
                        ),
                        true
                ))
                .orElse(new ContextResolution(initialMatch, false));
    }
}