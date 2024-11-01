package com.telegram_bot.PanDev.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

//конфигурационный класс бота изпользующий покет application.properties
@Configuration
@Data
@PropertySource("application.properties")
public class TelegramBotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String botToken;

}
