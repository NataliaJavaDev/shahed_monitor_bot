package com.tgbot.shahedmonitorbot.admin.dictionary;

import com.tgbot.shahedmonitorbot.monitoring.source.MonitoredSource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.UnaryOperator;

@Service
public class DictionaryConfigService {

	private final DictionaryStorage storage;
	private final DictionaryJsonService jsonService;

	public DictionaryConfigService(
		DictionaryStorage storage,
		DictionaryJsonService jsonService
	) {
		this.storage = storage;
		this.jsonService = jsonService;
	}

	public synchronized void update(
		UnaryOperator<DictionaryConfig> updater
	) {
		DynamicConfig current = storage.get();
		DictionaryConfig updated = updater.apply(current.dictionaries());

		storage.replace(new DynamicConfig(updated, List.copyOf(current.sources())));
		jsonService.save();
	}
}