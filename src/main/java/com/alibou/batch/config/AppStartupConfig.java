package com.alibou.batch.config;

import com.alibou.batch.registeration.RegistrationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AppStartupConfig {

    private final RegistrationService registrationService;

    @Bean
    public CommandLineRunner serviceRegistrationRunner() {
        return args -> {
            log.info("Initializing service registration...");
            registrationService.registerService();
        };
    }
}
