package com.tgbot.shahedmonitorbot.tdlib;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;


@Service
public class TdLibAuthorizationService {

    private final TdLibClientService tdLibClientService;
    private final AppProperties appProperties;
    private final TdLibUpdateHandler tdLibUpdateHandler;
    private final TemporaryHistoryExportService temporaryHistoryExportService;
    private final TdLibHistoryRequestService tdLibHistoryRequestService;
    private final TdLibStatusService tdLibStatusService;
    

    public TdLibAuthorizationService(
            TdLibClientService tdLibClientService,
            AppProperties appProperties,
            TdLibUpdateHandler tdLibUpdateHandler,
            TemporaryHistoryExportService temporaryHistoryExportService,
            TdLibHistoryRequestService tdLibHistoryRequestService,
            TdLibStatusService tdLibStatusService
    ) {
        this.tdLibClientService = tdLibClientService;
        this.appProperties = appProperties;
        this.tdLibUpdateHandler = tdLibUpdateHandler;
        this.temporaryHistoryExportService = temporaryHistoryExportService;
        this.tdLibHistoryRequestService = tdLibHistoryRequestService;
        this.tdLibStatusService = tdLibStatusService;
    }

    @PostConstruct
    public void startAuthorization() {
        Thread thread = new Thread(this::authorizationLoop);
        thread.setName("tdlib-authorization-thread");
        thread.start();

        Thread chatRequestThread = new Thread(() -> {
            try {
                Thread.sleep(5000);
                System.out.println("Requesting chats after startup...");
                requestChats();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        chatRequestThread.setName("tdlib-chat-request-thread");
        chatRequestThread.start();
    }

    private void authorizationLoop() {

        while (true) {

            String update = tdLibClientService.receive(1.0);

            if (update == null) {
                continue;
            }

            tdLibUpdateHandler.handle(update);
            temporaryHistoryExportService.handle(update);
            tdLibHistoryRequestService.handle(update);

            if (update.contains("\"@type\":\"error\"")) {
                System.out.println("TDLIB ERROR:");
                System.out.println(update);
            }

            if (update.contains("GET_CHATS")) {
                System.out.println("GET_CHATS_RESPONSE received");
            }

            if (update.contains("\"@type\":\"chats\"")) {
                System.out.println("TDLIB_CHATS_LIST received");
                requestChatDetails(update);
            }

            if (update.contains("\"authorizationStateWaitTdlibParameters\"")) {
                sendTdlibParameters();
            }

            if (update.contains("\"authorizationStateWaitEncryptionKey\"")) {
                tdLibClientService.send("""
                        {
                        "@type": "checkDatabaseEncryptionKey",
                        "encryption_key": ""
                        }
                        """);
            }

            if (update.contains("\"authorizationStateWaitPhoneNumber\"")) {
                sendPhoneNumber();
            }

            if (update.contains("\"authorizationStateWaitCode\"")) {
                String code = appProperties.tdlib().authCode();

                if (code == null || code.isBlank()) {
                    System.out.println("TDLib waits for auth code, but TDLIB_AUTH_CODE is empty.");
                    continue;
                }

                tdLibClientService.send("""
                        {
                        "@type": "checkAuthenticationCode",
                        "code": "%s"
                        }
                        """.formatted(code));
            }

            if (update.contains("\"authorizationStateReady\"")) {
                tdLibStatusService.markReady();

                System.out.println("TDLib authorization ready!");
                requestChats();
                temporaryHistoryExportService.startExport();
            }

            if (update.contains("\"authorizationStateClosing\"")
                    || update.contains("\"authorizationStateClosed\"")
                    || update.contains("\"authorizationStateLoggingOut\"")) {

                tdLibStatusService.markNotReady();

                System.out.println("TDLib authorization is not ready.");
            }
        }
    }

    private void sendTdlibParameters() {
        var tdlib = appProperties.tdlib();

        tdLibClientService.send("""
                {
                  "@type": "setTdlibParameters",
                  "database_directory": "%s",
                  "files_directory": "%s",
                  "use_message_database": true,
                  "use_secret_chats": false,
                  "api_id": %d,
                  "api_hash": "%s",
                  "system_language_code": "uk",
                  "device_model": "Desktop",
                  "application_version": "1.0",
                  "enable_storage_optimizer": true
                }
                """.formatted(
                tdlib.databaseDirectory(),
                tdlib.filesDirectory(),
                tdlib.apiId(),
                tdlib.apiHash()
        ));
    }

    private void sendPhoneNumber() {
        var tdlib = appProperties.tdlib();

        tdLibClientService.send("""
                {
                  "@type": "setAuthenticationPhoneNumber",
                  "phone_number": "%s"
                }
                """.formatted(tdlib.phoneNumber()));
    }

    private void requestChats() {

        System.out.println("Requesting chats...");

        tdLibClientService.send("""
            {
            "@type":"getChats",
            "@extra":"GET_CHATS",
            "chat_list":{
                "@type":"chatListMain"
            },
            "limit":100
            }
            """);
    }

    private void requestChatDetails(String update) {

        int start = update.indexOf("\"chat_ids\":[");
        if (start == -1) {
            return;
        }

        start = update.indexOf("[", start);
        int end = update.indexOf("]", start);

        if (start == -1 || end == -1) {
            return;
        }

        String ids = update.substring(start + 1, end);

        if (ids.isBlank()) {
            System.out.println("No chats found.");
            return;
        }

        for (String rawId : ids.split(",")) {
            String chatId = rawId.trim();

                tdLibClientService.send("""
                    {
                        "@type": "getChat",
                        "chat_id": %s
                    }
                    """.formatted(chatId));
        }
    }
}