package com.tgbot.shahedmonitorbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.tgbot.shahedmonitorbot.config.AppProperties;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(AppProperties.class)
public class ShahedMonitorBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShahedMonitorBotApplication.class, args);
	}
}
