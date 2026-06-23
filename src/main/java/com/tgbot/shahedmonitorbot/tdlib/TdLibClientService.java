package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;

@Service
public class TdLibClientService {

    private final TdJsonLibrary tdJsonLibrary;
    private final int clientId;

    public TdLibClientService(TdJsonLibraryLoader loader) {
        this.tdJsonLibrary = loader.getLibrary();

        this.clientId = tdJsonLibrary.td_create_client_id();
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
        tdJsonLibrary.td_send(clientId, request);
    }

    public String receive(double timeoutSeconds) {
        return tdJsonLibrary.td_receive(timeoutSeconds);
    }

    public String execute(String request) {
        return tdJsonLibrary.td_execute(request);
    }
}