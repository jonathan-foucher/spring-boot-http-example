package com.jonathanfoucher.httpexample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class HttpExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpExampleApplication.class, args);
    }
}
