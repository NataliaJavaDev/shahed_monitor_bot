package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryConfig;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryConfigService;
import com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryStorage;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectionAdminService {

	private final DictionaryStorage storage;
	private final DictionaryConfigService configService;

	public DirectionAdminService(
		DictionaryStorage storage,
		DictionaryConfigService configService
	) {
		this.storage = storage;
		this.configService = configService;
	}

	public synchronized List<String> getDirections() {

		return List.copyOf(
			storage.get()
				.dictionaries()
				.directions()
		);
	}

	public synchronized boolean addDirection(String direction) {

		String normalized = TextNormalizer.normalize(direction);

		if (normalized.isBlank()) {
			return false;
		}

		List<String> directions = new ArrayList<>(
			storage.get().dictionaries().directions()
		);

		if (directions.contains(normalized)) {
			return false;
		}

		directions.add(normalized);

		configService.update(current -> new DictionaryConfig(
			current.targets(),
			current.locations(),
			List.copyOf(directions),
			current.attention(),
			current.globalThreat(),
			current.forecast(),
			current.noise(),
			current.messageIntents()
		));

		return true;
	}

	public synchronized boolean removeDirection(String direction) {

		String normalized = TextNormalizer.normalize(direction);

		List<String> directions = new ArrayList<>(
			storage.get().dictionaries().directions()
		);

		if (!directions.remove(normalized)) {
			return false;
		}

		configService.update(current -> new DictionaryConfig(
			current.targets(),
			current.locations(),
			List.copyOf(directions),
			current.attention(),
			current.globalThreat(),
			current.forecast(),
			current.noise(),
			current.messageIntents()
		));

		return true;
	}
}