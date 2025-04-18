package com.lms.ccrp.service;

import com.lms.ccrp.dto.*;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.JwtToken;
import com.lms.ccrp.entity.RequestTransactions;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.RequestTransactionsRepository;
import com.lms.ccrp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenRepository jwtTokenRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;
    @Autowired
    private RequestTransactionsRepository requestTransactionsRepository;

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
        if (loginDTO.getUserType().equalsIgnoreCase(user.getRole().toString()))
            return jwtService.createToken(user);
        throw new RuntimeException("User not found.");
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

    public UserDashboardDTO fetchUserDashboard(String token) throws Exception {
        Optional<JwtToken> jwtToken = jwtTokenRepository.findByToken(token);
        if (jwtToken.isPresent()) {
            Customer customer = customerRepository.findByUserId(jwtToken.get().getUserId())
                    .orElseThrow(() -> new Exception("User not found"));
            UserDashboardDTO udDTO = new UserDashboardDTO();
            udDTO.setName(customer.getName());
            udDTO.setPoints(customer.getTotalPoints());
            List<RecentActivityDTO> raDTOList = new ArrayList<>();
            List<RequestTransactions> rtList = requestTransactionsRepository.findByCustomerIdAndIsCompletedTrue(customer.getCustomerId());
            for (RequestTransactions rt : rtList) {
                RecentActivityDTO dto = new RecentActivityDTO();

                String requestType = switch (rt.getTypeOfRequest()) {
                    case EARNED -> "Earned";
                    case EXPIRED -> "Expired";
                    default -> "Redeemed";
                };

                dto.setRequestType(requestType);
                dto.setPointsUsed(Math.abs(rt.getNumberOfPoints()));
                dto.setRewardDescription(rt.getRewardDescription());
                dto.setRequestDate(rt.getTransactionTime());
                raDTOList.add(dto);
            }
            udDTO.setRecentActivity(raDTOList);
            return udDTO;
        }
        throw new RuntimeException("Session not valid.");
    }
}