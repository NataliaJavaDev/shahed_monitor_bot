package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.*;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class TargetAdminService {

	private final DictionaryStorage storage;
	private final DictionaryConfigService configService;

	public TargetAdminService(
		DictionaryStorage storage,
		DictionaryConfigService configService
	) {
		this.storage = storage;
		this.configService = configService;
	}

	public synchronized List<String> getTargets() {

		return storage.get()
			.dictionaries()
			.targets()
			.stream()
			.flatMap(category -> category.aliases().stream())
			.toList();
	}

	public synchronized List<String> getCategories() {

		return storage.get()
			.dictionaries()
			.targets()
			.stream()
			.map(DictionaryCategory::category)
			.toList();
	}

	public synchronized List<String> getAliasesByCategory(String category) {

		String normalizedCategory = TextNormalizer.normalize(category);

		return storage.get()
			.dictionaries()
			.targets()
			.stream()
			.filter(item -> TextNormalizer.normalize(item.category()).equals(normalizedCategory))
			.findFirst()
			.map(DictionaryCategory::aliases)
			.map(List::copyOf)
			.orElse(List.of());
	}

	public synchronized String getCategory(String target) {

		String normalizedTarget = TextNormalizer.normalize(target);

		return storage.get()
			.dictionaries()
			.targets()
			.stream()
			.filter(category -> category.aliases()
				.stream()
				.map(TextNormalizer::normalize)
				.anyMatch(normalizedTarget::equals)
			)
			.map(DictionaryCategory::category)
			.findFirst()
			.orElse(normalizedTarget);
	}

	public synchronized boolean addTarget(String target) {
		return addTarget(target, target);
	}

	public synchronized boolean addTarget(String target, String category) {

		String normalizedTarget = TextNormalizer.normalize(target);
		String normalizedCategory = TextNormalizer.normalize(category);

		if (normalizedTarget.isBlank() || normalizedCategory.isBlank()) {
			return false;
		}

		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().targets());

		boolean targetExists = categories.stream()
			.anyMatch(item -> item.aliases()
				.stream()
				.map(TextNormalizer::normalize)
				.anyMatch(normalizedTarget::equals)
			);

		if (targetExists) {
			return false;
		}

		for (int index = 0; index < categories.size(); index++) {

			DictionaryCategory current = categories.get(index);

			if (!TextNormalizer.normalize(current.category()).equals(normalizedCategory)) {
				continue;
			}

			List<String> aliases = new ArrayList<>(current.aliases());
			aliases.add(normalizedTarget);

			categories.set(index,
				new DictionaryCategory(
					current.category(),
					current.displayName(),
					List.copyOf(aliases)
				)
			);

			List<DictionaryCategory> updatedTargets = List.copyOf(categories);

			configService.update(currentConfig -> new DictionaryConfig(
				updatedTargets,
				currentConfig.locations(),
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
				List.of(normalizedTarget)
			)
		);

		List<DictionaryCategory> updatedTargets = List.copyOf(categories);

		configService.update(currentConfig -> new DictionaryConfig(
			updatedTargets,
			currentConfig.locations(),
			currentConfig.directions(),
			currentConfig.attention(),
			currentConfig.globalThreat(),
			currentConfig.forecast(),
			currentConfig.noise(),
			currentConfig.messageIntents()
		));

		return true;
	}

	public synchronized boolean removeTarget(String target) {

		String normalizedTarget = TextNormalizer.normalize(target);
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().targets());

		for (int index = 0; index < categories.size(); index++) {

			DictionaryCategory current = categories.get(index);
			List<String> aliases = new ArrayList<>(current.aliases());

			boolean removed = aliases.removeIf(
				alias -> TextNormalizer.normalize(alias).equals(normalizedTarget)
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

			List<DictionaryCategory> updatedTargets = List.copyOf(categories);

			configService.update(currentConfig -> new DictionaryConfig(
				updatedTargets,
				currentConfig.locations(),
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
			.targets()
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
	
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().targets());
	
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
	
		replaceTargets(categories);
		return true;
	}

	public synchronized boolean removeCategory(String category) {
	
		String normalizedCategory = TextNormalizer.normalize(category);
		List<DictionaryCategory> categories = new ArrayList<>(storage.get().dictionaries().targets());
		boolean removed = categories.removeIf(item -> TextNormalizer.normalize(item.category()).equals(normalizedCategory));
	
		if (!removed) {
			return false;
		}
	
		replaceTargets(categories);
		return true;
	}

	private void replaceTargets(List<DictionaryCategory> categories) {

		configService.update(currentConfig -> new DictionaryConfig(
			List.copyOf(categories),
			currentConfig.locations(),
			currentConfig.directions(),
			currentConfig.attention(),
			currentConfig.globalThreat(),
			currentConfig.forecast(),
			currentConfig.noise(),
			currentConfig.messageIntents()
		));
	}
}