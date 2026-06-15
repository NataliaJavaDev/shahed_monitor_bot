package com.tgbot.shahedmonitorbot.model.admin;

public enum AdminSessionState {

    IDLE,

    WAITING_FOR_NEW_KEYWORD,
    WAITING_FOR_REMOVE_KEYWORD,

    WAITING_FOR_NEW_SOURCE_ID,
    WAITING_FOR_NEW_SOURCE_TITLE,
    WAITING_FOR_REMOVE_SOURCE
}