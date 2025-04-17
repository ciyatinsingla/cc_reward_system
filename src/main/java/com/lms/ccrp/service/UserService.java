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
import org.springframework.transaction.annotation.Transactional;

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
        return userRepository.save(user);
    }

    public String login(LoginDTO loginDTO) throws Exception {
        User user = userRepository.findByEmail(loginDTO.getEmail())
                .orElseThrow(() -> new Exception("User not found"));
        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword()))
            throw new Exception("Invalid credentials");
        return jwtService.createToken(user);
    }

    public boolean matchPassword(PasswordResetDTO dto, User user) throws Exception {
        if (user == null)
            user = userRepository.findByEmail(dto.getEmail())
                    .orElseThrow(() -> new Exception("User not found"));
        return passwordEncoder.matches(dto.getNewPassword(), user.getPassword());
    }

    public void resetPassword(PasswordResetDTO dto, User user) throws Exception {
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }


    public void checkIfUserExistsOrNot(UserDTO dto) {
        Optional<User> existingUser = userRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new RuntimeException("User already exists with email: " + dto.getEmail());
        }
    }

    public User fetchUser(Long userId) {
        Optional<User> existingUser = userRepository.findById(userId);
        return existingUser.orElse(null);

    }

    @Transactional
    public void logout(String token) {
        Optional<JwtToken> jwtToken = jwtTokenRepository.findByToken(token);
        jwtToken.ifPresent(value -> jwtTokenRepository.delete(value));
    }
}