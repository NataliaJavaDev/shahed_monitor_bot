package com.tgbot.shahedmonitorbot.model.admin;

import com.tgbot.shahedmonitorbot.admin.enums.*;

public class AdminSession {

	private AdminSessionState state = AdminSessionState.IDLE;

	private DictionaryType dictionaryType;
	private DictionaryAction dictionaryAction;
	private String selectedCategory;
	private String pendingSourceId;

	public AdminSessionState getState() {
		return state;
	}

	public void setState(AdminSessionState state) {
		this.state = state;
	}

	public DictionaryType getDictionaryType() {
		return dictionaryType;
	}

	public void setDictionaryType(DictionaryType dictionaryType) {
		this.dictionaryType = dictionaryType;
	}

	public DictionaryAction getDictionaryAction() {
		return dictionaryAction;
	}

	public void setDictionaryAction(DictionaryAction dictionaryAction) {
		this.dictionaryAction = dictionaryAction;
	}

	public String getSelectedCategory() {
		return selectedCategory;
	}

	public void setSelectedCategory(String selectedCategory) {
		this.selectedCategory = selectedCategory;
	}

	public String getPendingSourceId() {
		return pendingSourceId;
	}

	public void setPendingSourceId(String pendingSourceId) {
		this.pendingSourceId = pendingSourceId;
	}
}