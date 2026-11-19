package com.tgbot.shahedmonitorbot.tdlib;

import com.tgbot.shahedmonitorbot.config.AppProperties;
import com.tgbot.shahedmonitorbot.tdlib.history.TdLibHistoryRequestService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TdLibAuthorizationService {

    private final TdLibClientService tdLibClientService;
    private final AppProperties appProperties;
    private final TdLibUpdateHandler tdLibUpdateHandler;
    private final TemporaryHistoryExportService temporaryHistoryExportService;
    private final TdLibHistoryRequestService tdLibHistoryRequestService;
    private final TdLibStatusService tdLibStatusService;
    private final TdLibAuthFileService tdLibAuthFileService;
    private final ObjectMapper objectMapper;
    private final TdLibAdminChatDiscoveryService tdLibAdminChatDiscoveryService;

    private static final Logger log = LoggerFactory.getLogger(TdLibAuthorizationService.class);
    

    public TdLibAuthorizationService(
        TdLibClientService tdLibClientService,
        AppProperties appProperties,
        TdLibUpdateHandler tdLibUpdateHandler,
        TemporaryHistoryExportService temporaryHistoryExportService,
        TdLibHistoryRequestService tdLibHistoryRequestService,
        TdLibStatusService tdLibStatusService,
        TdLibAuthFileService tdLibAuthFileService,
        ObjectMapper objectMapper,
        TdLibAdminChatDiscoveryService tdLibAdminChatDiscoveryService
    ) {
        this.tdLibClientService = tdLibClientService;
        this.appProperties = appProperties;
        this.tdLibUpdateHandler = tdLibUpdateHandler;
        this.temporaryHistoryExportService = temporaryHistoryExportService;
        this.tdLibHistoryRequestService = tdLibHistoryRequestService;
        this.tdLibStatusService = tdLibStatusService;
        this.tdLibAuthFileService = tdLibAuthFileService;
        this.objectMapper = objectMapper;
        this.tdLibAdminChatDiscoveryService = tdLibAdminChatDiscoveryService;
    }

    @PostConstruct
    public void startAuthorization() {
        Thread thread = new Thread(this::authorizationLoop);
        thread.setName("tdlib-authorization-thread");
        thread.start();
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
            tdLibAdminChatDiscoveryService.handle(update);

            if (update.contains("\"@type\":\"error\"")) {
                log.error("TDLib error: {}", update);
            }

            if (update.contains("\"@type\":\"chats\"")) {
                log.info("TDLib chats loaded");

                requestChatDetails(update);
                // temporaryHistoryExportService.startExport(java.time.Duration.ofHours(2));
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
                String code = tdLibAuthFileService.read().authCode();

                if (code == null || code.isBlank()) {
                    log.warn("TDLib is waiting for authentication code");
                    continue;
                }
                submitAuthenticationCode(code);
            }

            if (update.contains("\"authorizationStateWaitPassword\"")) {
                String password = tdLibAuthFileService.read().twoFactorPassword();

                if (password == null || password.isBlank()) {
                    log.warn("TDLib is waiting for 2FA password");
                    continue;
                }
                submitTwoFactorPassword(password);
            }

            if (update.contains("\"authorizationStateReady\"")) {
                tdLibStatusService.markReady();
                log.info("TDLib authorization ready");

                tdLibAdminChatDiscoveryService.start();
                requestChats();
            }

            if (update.contains("\"authorizationStateClosing\"")
                    || update.contains("\"authorizationStateClosed\"")
                    || update.contains("\"authorizationStateLoggingOut\"")) {

                tdLibStatusService.markNotReady();
                log.warn("TDLib authorization is not ready");
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
        String phoneNumber = tdLibAuthFileService.read().phoneNumber();

        if (phoneNumber == null || phoneNumber.isBlank()) {
            log.warn("TDLib phone number is empty");
            return;
        }
        submitPhoneNumber(phoneNumber);
    }

    private void requestChats() {

        log.debug("Requesting TDLib chats");

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
            log.warn("TDLib returned no chats");
            return;
        }

        for (String rawId : ids.split(",")) {
            String chatId = rawId.trim();

                tdLibClientService.send("""
                    {
                        "@type": "getChat",
                        "chat_id": %s,
                        "@extra": "TDLIB_ADMIN_DISCOVERY_GET_CHAT"
                    }
                """.formatted(chatId));
        }
    }


    public void submitPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        String value = phoneNumber.trim();
        tdLibAuthFileService.savePhoneNumber(value);

        tdLibClientService.send("""
            {
            "@type": "setAuthenticationPhoneNumber",
            "phone_number": %s
            }
        """.formatted(toJson(value)));

        log.info("TDLib phone number submitted");
    }

    public void submitAuthenticationCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Authentication code cannot be empty");
        }

        String value = code.trim();
        tdLibAuthFileService.saveAuthCode(value);

        tdLibClientService.send("""
                {
                "@type": "checkAuthenticationCode",
                "code": %s
                }
            """.formatted(toJson(value)));

        log.info("TDLib authentication code submitted");
    }

    public void submitTwoFactorPassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("2FA password cannot be empty");
        }

        tdLibAuthFileService.saveTwoFactorPassword(password);

        tdLibClientService.send("""
                {
                "@type": "checkAuthenticationPassword",
                "password": %s
                }
            """.formatted(toJson(password)));

        log.info("TDLib 2FA password submitted");
    }

    private String toJson(String value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot encode TDLib request value", e);
        }
    }
}