package com.tgbot.shahedmonitorbot.tdlib;

import org.springframework.stereotype.Service;

@Service
public class TdLibStatusService {

    private volatile boolean ready = false;

    public boolean isReady() {
        return ready;
    }

    public void markReady() {
        ready = true;
    }

    public void markNotReady() {
        ready = false;
    }
}