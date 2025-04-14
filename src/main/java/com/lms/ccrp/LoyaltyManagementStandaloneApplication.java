package com.lms.ccrp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class LoyaltyManagementStandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyManagementStandaloneApplication.class, args);
    }

}
