package com.lms.ccrp.controller;

import com.lms.ccrp.config.JwtUtil;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.service.CustomUserDetailsService;
import com.lms.ccrp.service.OTPService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/otp")
@RequiredArgsConstructor
public class OTPController {

    private final UserRepository userRepository;
    private final CustomUserDetailsService userDetailsService;
    private final JwtUtil jwtUtil;
    private final OTPService otpService;

    @PostMapping("/request")
    public ResponseEntity<String> requestOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return ResponseEntity.status(404).body("User not found");

        otpService.generateOtp(email);
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody Map<String, String> body) {
        String email = body.get("email");
        String otp = body.get("otp");

        if (otpService.validateOtp(email, otp)) {
            otpService.clearOtp(email);
            UserDetails userDetails = userDetailsService.loadUserByUsername(userRepository.findByEmail(email).get().getUsername());
            String token = jwtUtil.generateToken(userDetails);
            return ResponseEntity.ok(token);
        }
        return ResponseEntity.status(401).body("Invalid OTP");
    }
}
