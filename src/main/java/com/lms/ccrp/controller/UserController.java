package com.lms.ccrp.controller;

import com.lms.ccrp.dto.LoginDTO;
import com.lms.ccrp.dto.PasswordResetDTO;
import com.lms.ccrp.dto.UserDTO;
import com.lms.ccrp.dto.UserDashboardDTO;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.UserRepository;
import com.lms.ccrp.service.JwtService;
import com.lms.ccrp.service.UPRService;
import com.lms.ccrp.service.UserService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Optional;

@Log4j2
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UPRService UPRService;

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Autowired
    private UserRepository userRepository;

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

    @PostMapping("/verify-password")
    public ResponseEntity<?> verifyPassword(@RequestBody PasswordResetDTO dto) {
        String email = dto.getEmail();
        String otp = dto.getOtp();
        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("Email not registered with us."));
            if (UPRService.validateOTP(email, otp)) {
                userService.resetPassword(dto, user);
                UPRService.clearOTP(email);
                return ResponseEntity.ok(Collections.singletonMap("token", jwtService.createToken(user)));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
        return ResponseEntity.status(401).body("Invalid OTP");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody PasswordResetDTO dto) {
        log.info("Reset Password request received.");
        String email = dto.getEmail();
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) return ResponseEntity.status(404).body("Email not registered with us.");
            UPRService.generateOTP(email);
            return ResponseEntity.ok("OTP sent to email");
        } catch (Exception e) {
            if (e instanceof MailAuthenticationException || e instanceof MailSendException)
                return ResponseEntity.badRequest().body("Unable to send email.");
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/dashboard")
    public ResponseEntity<UserDashboardDTO> userDashboard(@RequestHeader("Authorization") String token) {
        try {
            String authToken = token.replace("Bearer ", "");
            UserDashboardDTO dto = userService.fetchUserDashboard(authToken);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error in user dashboard: " + e.getMessage());
        }
    }
}