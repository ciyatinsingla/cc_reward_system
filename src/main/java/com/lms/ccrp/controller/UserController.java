package com.lms.ccrp.controller;

import com.lms.ccrp.dto.CustomerDTO;
import com.lms.ccrp.dto.LoginDTO;
import com.lms.ccrp.dto.PasswordResetDTO;
import com.lms.ccrp.dto.UserDTO;
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
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO dto) throws Exception {
        userService.checkIfUserExistsOrNot(dto);
        User user = userService.createUser(dto);
        try {
            String token = userService.login(new LoginDTO(dto.getEmail(), dto.getPassword()));
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto) {
        try {
            String token = userService.login(dto);
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            userService.logout(token);
            return ResponseEntity.ok("Logged out successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error logging out: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO dto, @RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            }

            Optional<JwtToken> jwtTokenOptional = jwtTokenRepository.findByToken(token);
            if (jwtTokenOptional.isEmpty() || jwtTokenOptional.get().getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token expired or not found");
            }

            Long userId = jwtService.extractUserId(token);
            userService.resetPassword(dto);
            return ResponseEntity.ok("Password updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}