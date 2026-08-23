package com.tgbot.shahedmonitorbot.admin.dictionary;

import java.util.List;

public record DictionaryCategory(
    String category,
    String displayName,
    List<String> aliases
) {
}