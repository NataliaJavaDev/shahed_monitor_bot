package com.tgbot.shahedmonitorbot.tdlib;

public record TdMessageContent(
    String text,
    Integer photoFileId
) {
}