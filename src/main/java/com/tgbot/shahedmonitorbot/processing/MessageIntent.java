package com.tgbot.shahedmonitorbot.processing;

public enum MessageIntent {

    NEW_EVENT,
    COUNT_UPDATE,
    ROUTE_UPDATE,
    LOCATION_UPDATE,
    STATUS_UPDATE,
    ATTENTION,
    UNKNOWN,
    THREAT_DETECTED,
    GLOBAL_THREAT,
    FORECAST
}