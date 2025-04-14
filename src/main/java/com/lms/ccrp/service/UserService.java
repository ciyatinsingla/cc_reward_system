package com.lms.ccrp.service;

import com.lms.ccrp.dto.LoginDTO;
import com.lms.ccrp.dto.PasswordResetDTO;
import com.lms.ccrp.dto.UserDTO;
import com.lms.ccrp.entity.JwtToken;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenRepository jwtTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        user.setRole(userDTO.getRole());
        user.setTotalPoints(0);
        return userRepository.save(user);
    }

    public String login(LoginDTO loginDTO) throws Exception {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new Exception("User not found"));
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new Exception("Invalid credentials");
        }
        String jwt = jwtService.generateToken(user);

        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(jwt);
        jwtToken.setUserId(user.getId());
        jwtToken.setExpiryDate(LocalDateTime.now().plusMinutes(60));
        jwtTokenRepository.save(jwtToken);
        return jwt;
    }

    public void resetPassword(PasswordResetDTO dto) throws Exception {
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new Exception("User not found"));
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }


    public void checkIfUserExistsOrNot(UserDTO dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists with email: " + dto.getEmail());
        }
    }
}