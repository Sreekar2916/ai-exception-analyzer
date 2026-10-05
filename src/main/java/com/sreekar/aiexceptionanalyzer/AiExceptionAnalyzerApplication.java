package com.sreekar.aiexceptionanalyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AiExceptionAnalyzerApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiExceptionAnalyzerApplication.class, args);
    }

}
