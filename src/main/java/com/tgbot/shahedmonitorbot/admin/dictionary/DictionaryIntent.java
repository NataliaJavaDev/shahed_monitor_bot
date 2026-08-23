package com.tgbot.shahedmonitorbot.admin.dictionary;

import java.util.List;

public record DictionaryIntent(
    String intent,
    List<String> aliases
) {
}