package com.sreekar.aiexceptionanalyzer.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "groq")
public record AIConfig(
        String apiKey,
        String baseUrl,
        String model,
        Duration timeout
) {
}
