package com.tgbot.shahedmonitorbot.monitoring.reason;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class AlertReasonResolverService {

    public AlertReason resolve(List<AlertReasonItem> items) {

        Map<String, LinkedHashSet<String>> grouped = new LinkedHashMap<>();

        for (AlertReasonItem item : items) {

            if (item == null) {
                continue;
            }

            grouped.computeIfAbsent(item.category(), key -> new LinkedHashSet<>())
                .addAll(item.threats());
        }

        List<AlertReasonItem> result = new ArrayList<>();

        grouped.forEach((category, threats) ->
            result.add(new AlertReasonItem(category, Set.copyOf(threats)))
        );

        return new AlertReason(result);
    }
}