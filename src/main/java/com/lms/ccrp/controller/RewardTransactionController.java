package com.lms.ccrp.controller;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.enums.RequestType;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.service.RewardTransactionService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardTransactionController {

    private final RewardTransactionService rewardTransactionService;

    @PostMapping("/admin/transactions/upload")
    public ResponseEntity<String> uploadAdminTransactions(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {

        try {
            Map<String, Long> userMap = rewardTransactionService.getRequesterId(authHeader);
            if (MapUtils.isEmpty(userMap))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            if (!userMap.containsKey(Role.ADMIN.name()))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Action not allowed");

            String adminRequesterId = Role.ADMIN.name() + userMap.get(Role.ADMIN.name());
            List<RewardTransactionHistoryDTO> dtoList = rewardTransactionService.parseExcelFile(file);
            rewardTransactionService.performTransactions(dtoList, adminRequesterId);
            return ResponseEntity.ok("Admin transactions from Excel processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    // User performs transactions
    @PostMapping("/customer/rewardRedemption")
    public ResponseEntity<String> performTransactionsByUser(@RequestBody RewardTransactionHistoryDTO dto, @RequestHeader("Authorization") String authHeader) {
        try {
            Map<String, Long> userMap = rewardTransactionService.getRequesterId(authHeader);
            if (MapUtils.isEmpty(userMap))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");

            if (userMap.containsKey(Role.USER.name()) &&
                    StringUtils.equalsAnyIgnoreCase(RequestType.REDEMPTION.name(), dto.getTypeOfRequest().name())) {
                String userRequesterId = Role.USER.name() + userMap.get(Role.USER.name());
                rewardTransactionService.performTransactions(List.of(dto), userRequesterId);
                return ResponseEntity.ok("User transactions processed successfully.");
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Request type not allowed.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    // Get total points of a user
    @GetMapping("/customer/{customerId}/points")
    public ResponseEntity<Integer> getUserPoints(@PathVariable Long customerId) {
        try {
            int points = rewardTransactionService.getCustomerPoints(customerId);
            return ResponseEntity.ok(points);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/customer/{customerId}/transactions")
    public ResponseEntity<List<RewardTransactionHistoryDTO>> getCustomerTransactions(@PathVariable Long customerId) {
        try {
            List<RewardTransactionHistoryDTO> transactions = rewardTransactionService.getCustomerTransactions(customerId);
            return ResponseEntity.ok(transactions);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
