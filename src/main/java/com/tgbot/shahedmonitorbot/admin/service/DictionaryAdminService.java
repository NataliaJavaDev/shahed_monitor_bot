package com.tgbot.shahedmonitorbot.admin.service;

import com.tgbot.shahedmonitorbot.admin.enums.DictionaryAction;
import com.tgbot.shahedmonitorbot.admin.enums.DictionaryType;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DictionaryAdminService {

	private final TargetAdminService targetAdminService;
	private final LocationAdminService locationAdminService;
	private final DirectionAdminService directionAdminService;

	public DictionaryAdminService(
		TargetAdminService targetAdminService,
		LocationAdminService locationAdminService,
		DirectionAdminService directionAdminService
	) {
		this.targetAdminService = targetAdminService;
		this.locationAdminService = locationAdminService;
		this.directionAdminService = directionAdminService;
	}

	public List<String> getValues(DictionaryType type) {

		return switch (type) {
			case TARGETS -> targetAdminService.getTargets();
			case LOCATIONS -> locationAdminService.getLocations();
			case DIRECTIONS -> directionAdminService.getDirections();
			default -> List.of();
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

	public boolean add(
		DictionaryType type,
		String value
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.addTarget(value);
			case LOCATIONS -> locationAdminService.addLocation(value);
			case DIRECTIONS -> directionAdminService.addDirection(value);
			default -> false;
		};
	}

	public boolean remove(
		DictionaryType type,
		String value
	) {

		return switch (type) {
			case TARGETS -> targetAdminService.removeTarget(value);
			case LOCATIONS -> locationAdminService.removeLocation(value);
			case DIRECTIONS -> directionAdminService.removeDirection(value);
			default -> false;
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
}