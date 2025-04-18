package com.lms.ccrp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableWebSecurity
public class LoyaltyManagementStandaloneApplication {

    public static void main(String[] args) {
        SpringApplication.run(LoyaltyManagementStandaloneApplication.class, args);
    }

}
