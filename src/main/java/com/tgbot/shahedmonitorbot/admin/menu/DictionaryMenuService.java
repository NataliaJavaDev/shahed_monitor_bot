package com.tgbot.shahedmonitorbot.admin.menu;

import com.tgbot.shahedmonitorbot.admin.enums.DictionaryType;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Service
public class DictionaryMenuService {

	public InlineKeyboardMarkup categoriesKeyboard(
		DictionaryType type,
		List<String> categories,
		boolean deleteMode
	) {
	
		List<InlineKeyboardRow> rows = new ArrayList<>();
		InlineKeyboardRow categoryRow = new InlineKeyboardRow();
	
		for (String category : categories) {
	
			String callback = deleteMode
				? "dictionary:delete-category:" + type.name() + ":" + category
				: "dictionary:category:" + type.name() + ":" + category;
	
			categoryRow.add(button(category, callback));
	
			if (categoryRow.size() == 2) {
				rows.add(categoryRow);
				categoryRow = new InlineKeyboardRow();
			}
		}
	
		if (!categoryRow.isEmpty()) {
			rows.add(categoryRow);
		}
	
		// rows.add(
		// 	singleRow(
		// 		getAddText(type),
		// 		"dictionary:add-category:" + type.name()
		// 	)
		// );
	
		// rows.add(
		// 	singleRow(
		// 		getRemoveText(type),
		// 		"dictionary:delete-mode:" + type.name()
		// 	)
		// );
	
		// rows.add(
		// 	singleRow(
		// 		"⬅️ Назад",
		// 		"dictionary:back"
		// 	)
		// );
	
		return markup(rows);
	}

	public InlineKeyboardMarkup deleteCategoriesKeyboard(
		DictionaryType type,
		List<String> categories
	) {

		List<InlineKeyboardRow> rows = new ArrayList<>();

		InlineKeyboardRow categoryRow = new InlineKeyboardRow();

		for (String category : categories) {

			categoryRow.add(
				button(
					"🗑 " + category,
					"dictionary:delete-category:"
						+ type.name()
						+ ":"
						+ category
				)
			);

			if (categoryRow.size() == 2) {
				rows.add(categoryRow);
				categoryRow = new InlineKeyboardRow();
			}
		}

		if (!categoryRow.isEmpty()) {
			rows.add(categoryRow);
		}

		rows.add(
			singleRow(
				"⬅️ Назад",
				"dictionary:categories:" + type.name()
			)
		);

		return markup(rows);
	}

	public InlineKeyboardMarkup categoryKeyboard(
		DictionaryType type,
		String category
	) {

		List<InlineKeyboardRow> rows = new ArrayList<>();

		// Показати аліаси
		rows.add(
			singleRow(
				"📋 Усі аліаси",
				"dictionary:aliases:"
					+ type.name()
					+ ":"
					+ category
			)
		);

		// Додати + Видалити аліас
		InlineKeyboardRow actionRow = new InlineKeyboardRow();

		actionRow.add(
			button(
				"➕ Додати аліас",
				"dictionary:add-alias:"
					+ type.name()
					+ ":"
					+ category
			)
		);

		actionRow.add(
			button(
				"➖ Видалити аліас",
				"dictionary:remove-alias:"
					+ type.name()
					+ ":"
					+ category
			)
		);

		rows.add(actionRow);

		// Назад
		rows.add(
			singleRow(
				"⬅️ Назад",
				"dictionary:categories:" + type.name()
			)
		);

		return markup(rows);
	}

	private String getAddText(DictionaryType type) {
		return "➕ Додати категорію";
	}
	
	private String getRemoveText(DictionaryType type) {
		return "➖ Видалити категорію";
	}

	private InlineKeyboardButton button(
		String text,
		String callbackData
	) {

		return InlineKeyboardButton.builder()
			.text(text)
			.callbackData(callbackData)
			.build();
	}

	private InlineKeyboardRow singleRow(
		String text,
		String callbackData
	) {

		InlineKeyboardRow row = new InlineKeyboardRow();

		row.add(
			button(text, callbackData)
		);

		return row;
	}

	private InlineKeyboardMarkup markup(
		List<InlineKeyboardRow> rows
	) {

		return InlineKeyboardMarkup.builder()
			.keyboard(rows)
			.build();
	}
}