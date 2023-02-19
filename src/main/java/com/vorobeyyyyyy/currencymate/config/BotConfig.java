package com.vorobeyyyyyy.currencymate.config;

import com.pengrad.telegrambot.TelegramBot;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@AllArgsConstructor
public class BotConfig {

    private final AppProperties properties;

    @Bean
    public TelegramBot bot() {
        return new TelegramBot(properties.getBot().getToken());
    }
}
