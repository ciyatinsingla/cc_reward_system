package com.lms.ccrp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableWebSecurity
@EnableAsync
public class LoyaltyManagementStandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyManagementStandaloneApplication.class, args);
    }

}
