package com.lms.ccrp.controller;

import com.lms.ccrp.dto.RewardTransactionHistoryDTO;
import com.lms.ccrp.enums.RequestType;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.service.RewardTransactionService;
import com.lms.ccrp.util.EmailSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequestMapping("/rewards")
@RequiredArgsConstructor
public class RewardTransactionController {

    private final RewardTransactionService rewardTransactionService;
    private final EmailSenderService emailSenderService;

    @PostMapping("/admin/transactions/upload")
    public ResponseEntity<String> uploadAdminTransactions(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {

        try {
            Map<String, Long> userMap = rewardTransactionService.getRequesterId(authHeader);
            if (MapUtils.isEmpty(userMap))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");
            if (!userMap.containsKey(Role.ADMIN.name()))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Action not allowed");
            List<RewardTransactionHistoryDTO> dtoList = rewardTransactionService.parseRTHFromExcelFile(file);
            rewardTransactionService.performRewardTransactions(dtoList);
            return ResponseEntity.ok("Admin transactions from Excel processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/customer/transaction")
    public ResponseEntity<String> performTransactionsByUser(@RequestBody RewardTransactionHistoryDTO transactionHistoryDTO, @RequestHeader("Authorization") String authHeader) {
        try {
            Map<String, Long> userMap = rewardTransactionService.getRequesterId(authHeader);
            if (MapUtils.isEmpty(userMap))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token");

            if (!(userMap.containsKey(Role.USER.name()) && StringUtils.equalsAnyIgnoreCase(RequestType.REDEMPTION.name(), transactionHistoryDTO.getTypeOfRequest().name())))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Request type not allowed.");

            Long userId = userMap.get(Role.USER.name());
            String userRequesterId = Role.USER.name() + userId;
            transactionHistoryDTO.setRequesterId(Role.USER.name() + userId);
            rewardTransactionService.performRewardTransactions(List.of(transactionHistoryDTO));
            log.info(emailSenderService.sendRedemptionEmailToUser(userId, transactionHistoryDTO));
            return ResponseEntity.ok("User transaction processed successfully.");

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/customer/{customerId}/points")
    public ResponseEntity<Long> getUserPoints(@PathVariable Long customerId) {
        try {
            long points = rewardTransactionService.getCustomerPoints(customerId);
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
