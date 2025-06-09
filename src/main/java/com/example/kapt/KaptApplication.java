package com.example.kapt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class KaptApplication {

    public static void main(String[] args) {
        SpringApplication.run(KaptApplication.class, args);
    }

}
