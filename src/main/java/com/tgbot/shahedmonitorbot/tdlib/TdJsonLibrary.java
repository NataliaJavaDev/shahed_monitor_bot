package com.tgbot.shahedmonitorbot.tdlib;

import com.sun.jna.Library;

public interface TdJsonLibrary extends Library {

    int td_create_client_id();

    void td_send(int clientId, String request);

    String td_receive(double timeout);

    String td_execute(String request);
}