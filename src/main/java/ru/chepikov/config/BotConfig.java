package ru.chepikov.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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

    @Bean
    public ThreadPoolTaskExecutor subscriptionCheckExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("subscription-check-");
        executor.initialize();
        return executor;
    }

    @Bean(destroyMethod = "shutdown")
    public Executor scheduledDeleting() {
        return Executors.newSingleThreadExecutor();
    }
}
