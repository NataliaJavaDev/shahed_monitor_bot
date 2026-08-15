package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TdLibClientService {

    private static final Logger log = LoggerFactory.getLogger(TdLibClientService.class);

    private final TdJsonLibrary tdJsonLibrary;
    private final int clientId;

    public TdLibClientService(TdJsonLibraryLoader loader) {
        this.tdJsonLibrary = loader.getLibrary();

        this.clientId = tdJsonLibrary.td_create_client_id();
        log.info("TDLib client created: {}", clientId);

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

    public void downloadFile(int fileId) {
        send("""
                {
                "@type": "downloadFile",
                "file_id": %d,
                "priority": 32,
                "offset": 0,
                "limit": 0,
                "synchronous": false
                }
                """.formatted(fileId));
    }
}