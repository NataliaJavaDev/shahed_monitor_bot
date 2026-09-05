package com.tgbot.shahedmonitorbot.admin.dictionary;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;

import java.util.List;

public record DynamicConfig(
    DictionaryConfig dictionaries,
    List<MonitoredSource> sources
) {
}