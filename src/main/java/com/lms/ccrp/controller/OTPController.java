package com.lms.ccrp.controller;

import com.lms.ccrp.dto.PasswordResetDTO;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.service.JwtService;
import com.lms.ccrp.service.OTPService;
import com.lms.ccrp.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Optional;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OTPController {

    private final UserRepository userRepository;
    private final OTPService otpService;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordResetDTO dto) {
        String email = dto.getEmail();
        String otp = dto.getOtp();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email not registered with us."));
            if (otpService.validateOTP(email, otp)) {
                userService.resetPassword(dto, user);
                otpService.clearOTP(email);
                return ResponseEntity.ok(Collections.singletonMap("token", jwtService.createToken(user)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(401).body("Invalid OTP");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO dto) {
        String email = dto.getEmail();
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Email not registered with us.");
            if (!userService.matchPassword(dto, userOpt.get())) {
                otpService.generateOTP(email);
                return ResponseEntity.ok("OTP sent to email");
            }
            return ResponseEntity.badRequest().body("New password must be different.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
