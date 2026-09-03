package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.dictionary.*;
import com.tgbot.shahedmonitorbot.admin.enums.*;
import com.tgbot.shahedmonitorbot.util.TextNormalizer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictionaryAdminService {

	private final TargetAdminService targetAdminService;
	private final LocationAdminService locationAdminService;
	private final DictionaryStorage storage;
	private final DictionaryConfigService configService;

	public DictionaryAdminService(
		TargetAdminService targetAdminService,
		LocationAdminService locationAdminService,
		DictionaryStorage storage,
		DictionaryConfigService configService
	) {
		this.targetAdminService = targetAdminService;
		this.locationAdminService = locationAdminService;
		this.storage = storage;
		this.configService = configService;
	}

	public List<String> getValues(DictionaryType type) {

		return switch (type) {
			case TARGETS -> targetAdminService.getTargets();
			case LOCATIONS -> locationAdminService.getLocations();
			case DIRECTIONS -> List.copyOf(storage.get().dictionaries().directions());
			case ATTENTION -> getAttentionAliases();
			case GLOBAL_THREAT -> List.copyOf(storage.get().dictionaries().globalThreat());
			case FORECAST -> List.copyOf(storage.get().dictionaries().forecast());
			case NOISE -> List.copyOf(storage.get().dictionaries().noise());
			case MESSAGE_INTENTS -> List.of();
		};
	}

	public List<String> getCategories(DictionaryType type) {

		return switch (type) {
			case TARGETS -> targetAdminService.getCategories();
			case LOCATIONS -> locationAdminService.getCategories();
			default -> List.of();
		};
	}

	public List<String> getAliases(DictionaryType type, String category) {

		return switch (type) {
			case TARGETS -> targetAdminService.getAliasesByCategory(category);
			case LOCATIONS -> locationAdminService.getAliasesByCategory(category);
			case ATTENTION -> getAttentionAliases();
			default -> List.of();
		};
	}

	public boolean addAlias(
		DictionaryType type,
		String category,
		String alias
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.addTarget(alias, category);
			case LOCATIONS -> locationAdminService.addLocation(alias, category);
			case ATTENTION -> addAttentionAlias(alias);
			default -> false;
		};
	}

	public boolean removeAlias(
		DictionaryType type,
		String category,
		String alias
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.removeTarget(alias);
			case LOCATIONS -> locationAdminService.removeLocation(alias);
			case ATTENTION -> removeAttentionAlias(alias);
			default -> false;
		};
	}

	public boolean addCategory(
		DictionaryType type,
		String category
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.addCategory(category);
			case LOCATIONS -> locationAdminService.addCategory(category);
			default -> false;
		};
	}

	public boolean removeCategory(
		DictionaryType type,
		String category
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.removeCategory(category);
			case LOCATIONS -> locationAdminService.removeCategory(category);
			default -> false;
		};
	}

	public boolean add(DictionaryType type, String value) {

		return switch (type) {
			case TARGETS -> targetAdminService.addTarget(value);
			case LOCATIONS -> locationAdminService.addLocation(value);
			case DIRECTIONS -> addSimpleValue(type, value);
			case ATTENTION -> addAttentionAlias(value);
			case GLOBAL_THREAT -> addSimpleValue(type, value);
			case FORECAST -> addSimpleValue(type, value);
			case NOISE -> addSimpleValue(type, value);
			case MESSAGE_INTENTS -> false;
		};
	}

	public boolean remove(
		DictionaryType type,
		String value
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.removeTarget(value);
			case LOCATIONS -> locationAdminService.removeLocation(value);
			case DIRECTIONS -> removeSimpleValue(type, value);
			case ATTENTION -> removeAttentionAlias(value);
			case GLOBAL_THREAT -> removeSimpleValue(type, value);
			case FORECAST -> removeSimpleValue(type, value);
			case NOISE -> removeSimpleValue(type, value);
			case MESSAGE_INTENTS -> false;
		};
	}

	public boolean execute(
		DictionaryType type,
		DictionaryAction action,
		String value
	) {

		return switch (action) {
			case ADD -> add(type, value);
			case REMOVE -> remove(type, value);
			case SHOW -> true;
		};
	}

	private List<String> getAttentionAliases() {

		return storage.get()
			.dictionaries()
			.attention()
			.stream()
			.flatMap(category -> category.aliases().stream())
			.toList();
	}

	private boolean addAttentionAlias(String alias) {

		String normalized = TextNormalizer.normalize(alias);

		if (normalized.isBlank()) {
			return false;
		}

		var categories = new ArrayList<>(
			storage.get().dictionaries().attention()
		);

		if (categories.isEmpty()) {
			return false;
		}

		var category = categories.getFirst();

		boolean exists = category.aliases()
			.stream()
			.map(TextNormalizer::normalize)
			.anyMatch(normalized::equals);

		if (exists) {
			return false;
		}

		List<String> aliases = new ArrayList<>(category.aliases());
		aliases.add(normalized);

		categories.set(
			0,
			new com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryCategory(
				category.category(),
				category.displayName(),
				List.copyOf(aliases)
			)
		);

		List<com.tgbot.shahedmonitorbot.admin.dictionary.DictionaryCategory> updated =
			List.copyOf(categories);

		configService.update(current -> new DictionaryConfig(
			current.targets(),
			current.locations(),
			current.directions(),
			updated,
			current.globalThreat(),
			current.forecast(),
			current.noise(),
			current.messageIntents()
		));

		return true;
	}

	private boolean removeAttentionAlias(String alias) {

		String normalized = TextNormalizer.normalize(alias);

		var categories = new ArrayList<>(
			storage.get().dictionaries().attention()
		);

		if (categories.isEmpty()) {
			return false;
		}

		var category = categories.getFirst();

		List<String> aliases = new ArrayList<>(category.aliases());

		boolean removed = aliases.removeIf(
			item -> TextNormalizer.normalize(item).equals(normalized)
		);

		if (!removed) {
			return false;
		}

		categories.set(
			0,
			new DictionaryCategory(
				category.category(),
				category.displayName(),
				List.copyOf(aliases)
			)
		);

		List<DictionaryCategory> updated = List.copyOf(categories);

		configService.update(current -> new DictionaryConfig(
			current.targets(),
			current.locations(),
			current.directions(),
			updated,
			current.globalThreat(),
			current.forecast(),
			current.noise(),
			current.messageIntents()
		));

		return true;
	}

	private boolean addSimpleValue(DictionaryType type, String value) {

		String normalized = TextNormalizer.normalize(value);

		if (normalized.isBlank()) {
			return false;
		}

		List<String> values = new ArrayList<>(getValues(type));

		if (values.stream()
			.map(TextNormalizer::normalize)
			.anyMatch(normalized::equals)) {
			return false;
		}

		values.add(normalized);
		updateSimpleDictionary(type, values);

		return true;
	}

	private boolean removeSimpleValue(DictionaryType type, String value) {

		String normalized = TextNormalizer.normalize(value);
		List<String> values = new ArrayList<>(getValues(type));
		boolean removed = values.removeIf(item -> TextNormalizer.normalize(item).equals(normalized));

		if (!removed) {
			return false;
		}

		updateSimpleDictionary(type, values);

		return true;
	}

	private void updateSimpleDictionary(DictionaryType type, List<String> values) {

		configService.update(current -> switch (type) {

			case DIRECTIONS -> new DictionaryConfig(
				current.targets(),
				current.locations(),
				List.copyOf(values),
				current.attention(),
				current.globalThreat(),
				current.forecast(),
				current.noise(),
				current.messageIntents()
			);

			case GLOBAL_THREAT -> new DictionaryConfig(
				current.targets(),
				current.locations(),
				current.directions(),
				current.attention(),
				List.copyOf(values),
				current.forecast(),
				current.noise(),
				current.messageIntents()
			);

			case FORECAST -> new DictionaryConfig(
				current.targets(),
				current.locations(),
				current.directions(),
				current.attention(),
				current.globalThreat(),
				List.copyOf(values),
				current.noise(),
				current.messageIntents()
			);

			case NOISE -> new DictionaryConfig(
				current.targets(),
				current.locations(),
				current.directions(),
				current.attention(),
				current.globalThreat(),
				current.forecast(),
				List.copyOf(values),
				current.messageIntents()
			);

			default -> current;
		});
	}
}