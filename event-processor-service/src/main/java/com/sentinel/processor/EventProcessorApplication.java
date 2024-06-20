package com.sentinel.processor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventProcessorApplication {
    public static void main(String[] args) {
        SpringApplication.run(EventProcessorApplication.class, args);
    }
}
