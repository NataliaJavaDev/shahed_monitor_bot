package com.tgbot.shahedmonitorbot.tdlib;

import com.sun.jna.Native;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

@Service
public class TdJsonLibraryLoader {

    private final TdJsonLibrary library;

    public TdJsonLibraryLoader(AppProperties appProperties) {
        this.library = Native.load(
                appProperties.tdlib().libraryPath(),
                TdJsonLibrary.class
        );
    }

    public TdJsonLibrary getLibrary() {
        return library;
    }
}