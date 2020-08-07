package com.example.mainpanel.api_implement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApplicationRun {
    public static void main(String[] args) {
        // need to pass args in here, Otherwise arguments can not be accessible from org.springframework.boot.ApplicationArguments
        SpringApplication.run(ApplicationRun.class, args);
    }
}