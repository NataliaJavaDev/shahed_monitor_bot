package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.*;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class LocationAdminService {

	private final DictionaryStorage storage;
	private final DictionaryConfigService configService;

	public LocationAdminService(
		DictionaryStorage storage,
		DictionaryConfigService configService
	) {
		this.storage = storage;
		this.configService = configService;
	}

	public synchronized List<String> getLocations() {

		return storage.get()
			.dictionaries()
			.locations()
			.stream()
			.flatMap(category -> category.aliases().stream())
			.toList();
	}

	public synchronized List<String> getCategories() {

		return storage.get()
			.dictionaries()
			.locations()
			.stream()
			.map(DictionaryCategory::category)
			.toList();
	}

	public synchronized List<String> getAliasesByCategory(String category) {
	
		String normalizedCategory = TextNormalizer.normalize(category);
	
		return storage.get()
			.dictionaries()
			.locations()
			.stream()
			.filter(item ->TextNormalizer.normalize(item.category()).equals(normalizedCategory))
			.findFirst()
			.map(DictionaryCategory::aliases)
			.map(List::copyOf)
			.orElse(List.of());
	}

	public synchronized String getCategory(String location) {

		String normalizedLocation = TextNormalizer.normalize(location);

		return storage.get()
			.dictionaries()
			.locations()
			.stream()
			.filter(category -> category.aliases()
				.stream()
				.map(TextNormalizer::normalize)
				.anyMatch(normalizedLocation::equals)
			)
			.map(DictionaryCategory::category)
			.findFirst()
			.orElse(normalizedLocation);
	}

	public synchronized boolean addLocation(String location) {
		return addLocation(location, location);
	}

	public synchronized boolean addLocation(String location, String category) {

		String normalizedLocation = TextNormalizer.normalize(location);
		String normalizedCategory = TextNormalizer.normalize(category);

		if (normalizedLocation.isBlank() || normalizedCategory.isBlank()) {
			return false;
		}

		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());

		boolean locationExists = categories.stream()
			.anyMatch(item -> item.aliases()
				.stream()
				.map(TextNormalizer::normalize)
				.anyMatch(normalizedLocation::equals)
			);

		if (locationExists) {
			return false;
		}

		for (int index = 0; index < categories.size(); index++) {

			DictionaryCategory current = categories.get(index);

			if (!TextNormalizer.normalize(current.category()).equals(normalizedCategory)) {
				continue;
			}

			List<String> aliases = new ArrayList<>(current.aliases());
			aliases.add(normalizedLocation);

			categories.set(index,
				new DictionaryCategory(
					current.category(),
					current.displayName(),
					List.copyOf(aliases)
				)
			);

			List<DictionaryCategory> updatedLocations = List.copyOf(categories);

			configService.update(currentConfig -> new DictionaryConfig(
				currentConfig.targets(),
				updatedLocations,
				currentConfig.directions(),
				currentConfig.attention(),
				currentConfig.globalThreat(),
				currentConfig.forecast(),
				currentConfig.noise(),
				currentConfig.messageIntents()
			));

			return true;
		}

		categories.add(
			new DictionaryCategory(
				normalizedCategory,
				normalizedCategory,
				List.of(normalizedLocation)
			)
		);

		List<DictionaryCategory> updatedLocations = List.copyOf(categories);

		configService.update(currentConfig -> new DictionaryConfig(
			currentConfig.targets(),
			updatedLocations,
			currentConfig.directions(),
			currentConfig.attention(),
			currentConfig.globalThreat(),
			currentConfig.forecast(),
			currentConfig.noise(),
			currentConfig.messageIntents()
		));

		return true;
	}

	public synchronized boolean removeLocation(String location) {

		String normalizedLocation = TextNormalizer.normalize(location);
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());

		for (int index = 0; index < categories.size(); index++) {

			DictionaryCategory current = categories.get(index);
			List<String> aliases = new ArrayList<>(current.aliases());

			boolean removed = aliases.removeIf(
				alias -> TextNormalizer.normalize(alias).equals(normalizedLocation)
			);

			if (!removed) {
				continue;
			}

			if (aliases.isEmpty()) {
				categories.remove(index);
			} else {
				categories.set(index,
					new DictionaryCategory(
						current.category(),
						current.displayName(),
						List.copyOf(aliases)
					)
				);
			}

			List<DictionaryCategory> updatedLocations = List.copyOf(categories);

			configService.update(currentConfig -> new DictionaryConfig(
				currentConfig.targets(),
				updatedLocations,
				currentConfig.directions(),
				currentConfig.attention(),
				currentConfig.globalThreat(),
				currentConfig.forecast(),
				currentConfig.noise(),
				currentConfig.messageIntents()
			));

			return true;
		}

		return false;
	}

	public synchronized String getDisplayName(String category) {
		
		String normalizedCategory = TextNormalizer.normalize(category);

		return storage.get()
			.dictionaries()
			.locations()
			.stream()
			.filter(item -> TextNormalizer.normalize(item.category()).equals(normalizedCategory))
			.map(DictionaryCategory::displayName)
			.filter(displayName -> displayName != null && !displayName.isBlank())
			.findFirst()
			.orElse(normalizedCategory);
	}

	public synchronized boolean addCategory(String category) {
	
		String normalizedCategory = TextNormalizer.normalize(category);
	
		if (normalizedCategory.isBlank()) {
			return false;
		}
	
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());
	
		boolean exists = categories.stream()
			.anyMatch(item -> TextNormalizer.normalize(item.category()).equals(normalizedCategory));
	
		if (exists) {
			return false;
		}
	
		categories.add(
			new DictionaryCategory(
				normalizedCategory,
				normalizedCategory,
				List.of()
			)
		);
	
		replaceLocations(categories);
		return true;
	}

	public synchronized boolean removeCategory(String category) {
	
		String normalizedCategory = TextNormalizer.normalize(category);
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().locations());
		boolean removed = categories.removeIf(item -> TextNormalizer.normalize(item.category()).equals(normalizedCategory));
	
		if (!removed) {
			return false;
		}
	
		replaceLocations(categories);
		return true;
	}

	private void replaceLocations(List<DictionaryCategory> categories) {

		configService.update(currentConfig -> new DictionaryConfig(
			currentConfig.targets(),
			List.copyOf(categories),
			currentConfig.directions(),
			currentConfig.attention(),
			currentConfig.globalThreat(),
			currentConfig.forecast(),
			currentConfig.noise(),
			currentConfig.messageIntents()
		));
	}
}