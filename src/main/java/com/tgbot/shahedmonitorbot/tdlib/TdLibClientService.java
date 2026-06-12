package com.tgbot.shahedmonitorbot.tdlib;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.sun.jna.Pointer;

@Service
public class TdLibClientService {

    private Pointer clientId;

    @PostConstruct
    public void start() {
        clientId = TdJsonLibrary.INSTANCE.td_create_client_id();

        System.out.println("TDLib client created: " + clientId);

        String response = TdJsonLibrary.INSTANCE.td_execute(
                "{\"@type\":\"getTextEntities\",\"text\":\"TDLib test\"}"
        );

        System.out.println("TDLib test response: " + response);
    }
}