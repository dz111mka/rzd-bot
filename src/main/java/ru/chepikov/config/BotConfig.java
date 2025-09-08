package ru.chepikov.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


@Getter
@Configuration
@EnableScheduling
@PropertySource("classpath:application.yml")
public class BotConfig {

    @Value("${bot.name}")
    String botName;

    @Value("${bot.token}")
    String token;

    @Bean(destroyMethod = "shutdown")
    public Executor scheduledChecking() {
        return Executors.newSingleThreadExecutor();
    }

    @Bean(destroyMethod = "shutdown")
    public Executor scheduledDeleting() {
        return Executors.newSingleThreadExecutor();
    }
}