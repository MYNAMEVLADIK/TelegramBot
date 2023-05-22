package com.example.telegrembot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TelegremBotApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelegremBotApplication.class, args);
    }

}
