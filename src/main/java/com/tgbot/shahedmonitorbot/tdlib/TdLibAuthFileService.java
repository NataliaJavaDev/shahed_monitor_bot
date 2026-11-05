
package com.tgbot.shahedmonitorbot.tdlib;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgbot.shahedmonitorbot.config.AppProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
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

    public AuthData read() {
        Path path = Path.of(appProperties.tdlib().authFile());

        try {
            return objectMapper.readValue(path.toFile(), AuthData.class);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Cannot read TDLib authentication file: " + path, e
            );
        }
    }

    public record AuthData(
            String phoneNumber,

            @JsonProperty("auth-code")
            String authCode,

            @JsonProperty("2FA")
            String twoFactorPassword
    ) {}
}
