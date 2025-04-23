package com.lms.ccrp.controller;

import com.lms.ccrp.dto.RewardHistoryDTO;
import com.lms.ccrp.entity.Customer;
import com.lms.ccrp.entity.Reward;
import com.lms.ccrp.enums.RewardRequestType;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.repository.CustomerRepository;
import com.lms.ccrp.repository.RewardRepository;
import com.lms.ccrp.service.RewardHistoryService;
import com.lms.ccrp.util.AuthUtil;
import com.lms.ccrp.util.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@Log4j2
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardHistoryController {

    private final RewardHistoryService rewardHistoryService;
    private final EmailSenderService emailSenderService;
    private final AuthUtil authUtil;
    private final RewardRepository rewardRepository;
    private final CustomerRepository customerRepository;

    @PostMapping("/redeem")
    public ResponseEntity<String> performTransactionsByUser(@RequestBody Reward reward, @RequestHeader("Authorization") String authHeader) {
        try {
            long userId = authUtil.authenticateUserAndFetchId(authHeader);
            Customer customer = customerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("Customer doesn't exist."));
            RewardHistoryDTO transactionHistoryDTO = getRewardHistoryDTO(reward, customer, userId);

            rewardHistoryService.performRewardTransactions(List.of(transactionHistoryDTO));
            log.info(emailSenderService.sendRedemptionRequestEmailToUser(userId, transactionHistoryDTO));
            return ResponseEntity.ok("User transaction processed successfully.");

        } catch (Exception e) {
            if(e.getMessage().contains("Authentication failed"))
                return ResponseEntity.ok("User transaction processed successfully.");
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    private static RewardHistoryDTO getRewardHistoryDTO(Reward reward, Customer customer, long userId) {
        RewardHistoryDTO transactionHistoryDTO = new RewardHistoryDTO();
        transactionHistoryDTO.setCustomerId(customer.getCustomerId());
        transactionHistoryDTO.setName(customer.getName());
        transactionHistoryDTO.setTypeOfRequest(RewardRequestType.REDEMPTION);
        transactionHistoryDTO.setDateOfBirth(customer.getDateOfBirth());
        transactionHistoryDTO.setTransactionTime(new Date());
        transactionHistoryDTO.setRewardDescription(reward.getRewardDescription().replaceAll("₹\\d+\\s*", ""));
        transactionHistoryDTO.setRequesterId(Role.USER.name() + userId);
        transactionHistoryDTO.setNumberOfPoints(reward.getNumberOfPoints());
        return transactionHistoryDTO;
    }

    @GetMapping
    public ResponseEntity<List<Reward>> getAllRewards() {
        return ResponseEntity.ok(rewardRepository.findByIsActiveTrue());
    }

}
