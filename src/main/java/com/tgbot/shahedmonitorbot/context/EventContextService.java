package com.tgbot.shahedmonitorbot.context;

import com.tgbot.shahedmonitorbot.processing.MonitorMatch;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventContextService {

    private MonitorMatch lastEvent;

    public void save(MonitorMatch match) {
        if (match == null) {
            return;
        }

        this.lastEvent = match;
    }

    public Optional<MonitorMatch> getLastEvent() {
        return Optional.ofNullable(lastEvent);
    }

    public void clear() {
        this.lastEvent = null;
    }
}