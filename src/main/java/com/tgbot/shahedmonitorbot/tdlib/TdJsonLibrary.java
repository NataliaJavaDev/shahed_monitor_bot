package com.tgbot.shahedmonitorbot.tdlib;

import com.sun.jna.Library;
import com.sun.jna.Native;

public interface TdJsonLibrary extends Library {

    TdJsonLibrary INSTANCE = Native.load(
        "/home/natta/tdlib-build/td/build/libtdjson.so",
        TdJsonLibrary.class
    );

    int td_create_client_id();

    void td_send(int clientId, String request);

    String td_receive(double timeout);

    String td_execute(String request);
}