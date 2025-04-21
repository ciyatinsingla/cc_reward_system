package com.lms.ccrp.service;

import com.lms.ccrp.dto.*;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.JwtToken;
import com.lms.ccrp.entity.SourceTransactions;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.JwtTokenRepository;
import com.lms.ccrp.repository.SourceTransactionsRepository;
import com.lms.ccrp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
    private SourceTransactionsRepository sourceTransactionsRepository;

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
            List<SourceTransactions> rtList = sourceTransactionsRepository.findByCustomerIdAndIsCompletedTrue(customer.getCustomerId());
            for (SourceTransactions rt : rtList) {
                RecentActivityDTO dto = fetchRecentActivityDTO(rt);
                raDTOList.add(dto);
            }
            udDTO.setRecentActivity(raDTOList);
            return udDTO;
        }
        throw new RuntimeException("Session not valid.");
    }

    /**
     * Fetches the admin dashboard data for a user authenticated by the given JWT token.
     * <p>
     * This method validates the provided token, verifies the user role as ADMIN,
     * and compiles an overview of customer activity including:
     * <ul>
     *   <li>List of all active customers</li>
     *   <li>Total points awarded to customers till date</li>
     *   <li>Recent transaction activities for each customer</li>
     * </ul>
     * The compiled data is returned as an {@link AdminDashboardDTO} object.
     *
     * @param token the JWT token used to authenticate the admin user
     * @return an {@link AdminDashboardDTO} containing customer statistics and activity details
     * @throws Exception if the token is invalid, expired, or if the user is not found
     *                   or if the user is not authorized as an admin
     */
    public AdminDashboardDTO fetchAdminDashboard(String token) throws Exception {
        Optional<JwtToken> jwtToken = jwtTokenRepository.findByToken(token);
        if (jwtToken.isEmpty())
            throw new RuntimeException("Token Invalid or Expired");
        User user = userRepository.findById(jwtToken.get().getUserId())
                .orElseThrow(() -> new Exception("User not found"));
        if (!user.getRole().equals(Role.ADMIN))
            throw new RuntimeException("Action not allowed");

        long tillDatePointsAwarded = 0;
        List<PointsManagementDTO> allCustomersDTOList = new ArrayList<>();
        List<Customer> allCustomers = customerRepository.findAllActiveCustomerIds();
        Map<Long, String> idEmailMap = allCustomers.stream()
                .collect(Collectors.toMap(
                        Customer::getCustomerId,
                        customer -> customer.getUser().getEmail()
                ));

        for (Customer customer : allCustomers) {
            tillDatePointsAwarded += customer.getTotalPoints();
            PointsManagementDTO pmDTO = PointsManagementDTO
                    .builder()
                    .customerId(customer.getCustomerId())
                    .name(customer.getName())
                    .email(idEmailMap.get(customer.getCustomerId()))
                    .points(customer.getTotalPoints())
                    .build();

            List<RecentActivityDTO> recentActivityList = new ArrayList<>();
            List<SourceTransactions> rtList = sourceTransactionsRepository.findByCustomerIdAndIsCompletedTrue(customer.getCustomerId());
            for (SourceTransactions rt : rtList)
                recentActivityList.add(fetchRecentActivityDTO(rt));
            pmDTO.setRecentActivity(recentActivityList);
            allCustomersDTOList.add(pmDTO);
        }

        return AdminDashboardDTO.builder()
                .allCustomersDTOList(allCustomersDTOList)
                .activeUsers(allCustomersDTOList.size())
                .totalPointsAwarded(tillDatePointsAwarded)
                .build();
    }

    /**
     * Fetches the recent activity details from the given request transaction.
     *
     * @param sourceTransactions the {@link SourceTransactions} object containing details of the request transaction.
     * @return object {@link RecentActivityDTO} object containing the transformed data.
     */
    private RecentActivityDTO fetchRecentActivityDTO(SourceTransactions sourceTransactions) {
        return RecentActivityDTO.builder()
                .requestType(switch (sourceTransactions.getTypeOfRequest()) {
                    case EARNED -> "Earned";
                    case EXPIRED -> "Expired";
                    default -> "Redeemed";
                })
                .pointsUsed(Math.abs(sourceTransactions.getNumberOfPoints()))
                .rewardDescription(sourceTransactions.getRewardDescription() != null ? sourceTransactions.getRewardDescription() : "No Description")
                .requestDate(sourceTransactions.getTransactionTime())
                .build();
    }

}