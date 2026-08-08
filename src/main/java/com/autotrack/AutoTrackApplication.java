package com.autotrack;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AutoTrackApplication {
    public static void main(String[] args) {
        SpringApplication.run(AutoTrackApplication.class, args);
    }
}
