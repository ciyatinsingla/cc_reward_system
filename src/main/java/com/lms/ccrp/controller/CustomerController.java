package com.lms.ccrp.controller;


import com.lms.ccrp.dto.CustomerDTO;
import com.lms.ccrp.entity.JwtToken;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.service.CustomerService;
import com.lms.ccrp.service.JwtService;
import com.lms.ccrp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/customer")
public class CustomerController {

    @Autowired
    private CustomerService custService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtTokenRepository tokenRepository;

    @PostMapping("/create/customer")
    public ResponseEntity<?> createNewCustomer(@RequestBody CustomerDTO dto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");

            Optional<JwtToken> jwtToken = tokenRepository.findByToken(token);

            if (jwtToken.isPresent()) {
                Long userId = jwtToken.get().getUserId();
                User user = userService.fetchUser(userId);
                if (user == null)
                    throw new RuntimeException("User not found for customer: " + dto.getName());
                Long customerId = custService.createCustomer(dto, user);
                return ResponseEntity.ok(Collections.singletonMap("customerId", customerId));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}
