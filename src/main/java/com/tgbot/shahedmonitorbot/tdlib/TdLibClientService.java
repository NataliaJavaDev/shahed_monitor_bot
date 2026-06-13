package com.tgbot.shahedmonitorbot.tdlib;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import com.sun.jna.Pointer;

@Service
public class TdLibClientService {

    private final int clientId;

    public TdLibClientService() {
        this.clientId = TdJsonLibrary.INSTANCE.td_create_client_id();
        System.out.println("TDLib client created: " + clientId);

        send("""
                {
                  "@type": "getAuthorizationState"
                }
                """);
    }

    public int getClientId() {
        return clientId;
    }

    public void send(String request) {
        TdJsonLibrary.INSTANCE.td_send(clientId, request);
    }

    public String receive(double timeoutSeconds) {
        return TdJsonLibrary.INSTANCE.td_receive(timeoutSeconds);
    }

    public String execute(String request) {
        return TdJsonLibrary.INSTANCE.td_execute(request);
    }
}