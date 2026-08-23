package com.tgbot.shahedmonitorbot.admin.dictionary;

import org.springframework.stereotype.Service;

@Service
public class DictionaryStorage {

    private DynamicConfig config;

    public synchronized DynamicConfig get() {
        return config;
    }

    public synchronized void replace(DynamicConfig config) {
        this.config = config;
    }
}