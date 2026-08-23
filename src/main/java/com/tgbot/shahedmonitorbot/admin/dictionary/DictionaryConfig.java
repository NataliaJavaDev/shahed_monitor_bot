package com.tgbot.shahedmonitorbot.admin.dictionary;

import java.util.List;

public record DictionaryConfig(
    List<DictionaryCategory> targets,
    List<DictionaryCategory> locations,
    List<String> directions,
    List<DictionaryCategory> attention,
    List<String> globalThreat,
    List<String> forecast,
    List<String> noise,
    List<DictionaryIntent> messageIntents
) {
}