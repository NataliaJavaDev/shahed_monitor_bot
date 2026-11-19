
package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
public class TdLibAuthFileService {

    private final ObjectMapper objectMapper;
    private final AppProperties appProperties;

    public TdLibAuthFileService(
        ObjectMapper objectMapper,
        AppProperties appProperties
    ) {
        this.objectMapper = objectMapper;
        this.appProperties = appProperties;
    }

    public synchronized AuthData read() {
        Path path = Path.of(appProperties.tdlib().authFile());

        try {
            return objectMapper.readValue(path.toFile(), AuthData.class);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read TDLib authentication file: " + path, e);
        }
    }

    public synchronized void write(AuthData authData) {
        Path path = Path.of(appProperties.tdlib().authFile());

        try {
            Path parent = path.getParent();

            if (parent != null) {
                Files.createDirectories(parent);
            }

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), authData);

        } catch (IOException e) {
            throw new IllegalStateException("Cannot save TDLib authentication file: " + path, e);
        }
    }

    public synchronized void savePhoneNumber(String phoneNumber) {
        AuthData current = read();
        write(new AuthData(phoneNumber, current.authCode(), current.twoFactorPassword()));
    }

    public synchronized void saveAuthCode(String authCode) {
        AuthData current = read();
        write(new AuthData(current.phoneNumber(), authCode, current.twoFactorPassword()));
    }

    public synchronized void saveTwoFactorPassword(String password) {
        AuthData current = read();
        write(new AuthData(current.phoneNumber(), current.authCode(), password));
    }

    public record AuthData(
        String phoneNumber,

        @JsonProperty("auth-code")
        String authCode,

        @JsonProperty("2FA")
        String twoFactorPassword
    ) {}
}
