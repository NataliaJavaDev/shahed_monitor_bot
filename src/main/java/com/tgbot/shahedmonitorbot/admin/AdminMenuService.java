package com.tgbot.shahedmonitorbot.admin;

import org.springframework.stereotype.Service;

@Service
public class AdminMenuService {

    public String mainMenuText() {
        return """
                Адмін-меню:
                
                /keywords — список ключових слів
                /add_keyword слово — додати ключове слово
                /remove_keyword слово — видалити ключове слово
                
                Або:
                /add_keyword — режим додавання
                /remove_keyword — режим видалення
                """;
    }
}