package com.lms.ccrp.controller;

import com.lms.ccrp.dto.LoginDTO;
import com.lms.ccrp.dto.UserDTO;
import com.lms.ccrp.dto.UserDashboardDTO;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.service.JwtService;
import com.lms.ccrp.service.OTPService;
import com.lms.ccrp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private OTPService otpService;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserDTO dto) throws Exception {
        userService.checkIfUserExistsOrNot(dto);
        User user = userService.createUser(dto);
        try {
            String token = userService.login(new LoginDTO(dto.getEmail(), dto.getPassword(), dto.getRole().toString().toLowerCase()));
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

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        try {
            String authToken = token.replace("Bearer ", "");
            userService.logout(authToken);
            return ResponseEntity.ok("Logged out successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error logging out: " + e.getMessage());
        }
    }

    @PostMapping("/dashboard")
    public ResponseEntity<UserDashboardDTO> userDashboard(@RequestHeader("Authorization") String token) {
        try {
            String authToken = token.replace("Bearer ", "");
            UserDashboardDTO dto = userService.fetchUserDashboard(authToken);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error logging out: " + e.getMessage());
        }
    }
}