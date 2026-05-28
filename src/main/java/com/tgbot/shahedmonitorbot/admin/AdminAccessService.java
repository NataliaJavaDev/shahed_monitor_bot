package com.tgbot.shahedmonitorbot.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminAccessService {

    public boolean isAdmin(Long userId) {
        return true;
    }
}