package com.tgbot.shahedmonitorbot.tdlib;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class TdLibAuthorizationService {

    private final TdLibClientService tdLibClientService;
    private final AppProperties appProperties;

    public TdLibAuthorizationService(
            TdLibClientService tdLibClientService,
            AppProperties appProperties
    ) {
        this.tdLibClientService = tdLibClientService;
        this.appProperties = appProperties;
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

        Scanner scanner = new Scanner(System.in);

       while (true) {

            String update = tdLibClientService.receive(1.0);

            if (update == null) {
                continue;
            }

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

            if (update.contains("\"@type\":\"chat\"")
                    && (update.contains("\"chatTypeBasicGroup\"")
                    || update.contains("\"chatTypeSupergroup\""))) {

                writeChatToFile(update);
                System.out.println("GROUP_OR_CHANNEL saved to tdlib-chats.log");

                if (update.contains("Дитяче")
                        || update.contains("дитяче")
                        || update.contains("Тест БЦ")
                        || update.contains("тест")
                        || update.contains("бот")) {

                    System.out.println("FOUND_TARGET_CHAT:");
                    System.out.println(update);
                }
            }

            if (update.contains("\"@type\":\"updateNewMessage\"")) {
                System.out.println("NEW MESSAGE:");
                System.out.println(update);
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
                System.out.print("Enter Telegram code: ");
                String code = scanner.nextLine();

                tdLibClientService.send("""
                        {
                        "@type": "checkAuthenticationCode",
                        "code": "%s"
                        }
                        """.formatted(code));
            }

            if (update.contains("\"authorizationStateReady\"")) {
                System.out.println("TDLib authorization ready!");
                requestChats();
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

    // private void requestChats() {

    //     System.out.println("Requesting chats...");

    //     tdLibClientService.send("""
    //             {
    //             "@type": "getChats",
    //             "chat_list": {
    //                 "@type": "chatListMain"
    //             },
    //             "limit": 100
    //             }
    //             """);
    // }

    private void writeChatToFile(String update) {

        try {
            Files.writeString(
                    Path.of("tdlib-chats.log"),
                    update + System.lineSeparator(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.out.println("Failed to write chat to file: " + e.getMessage());
        }
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