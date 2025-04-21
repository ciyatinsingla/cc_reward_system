package com.lms.ccrp.controller;

import com.lms.ccrp.dto.AdminDashboardDTO;
import com.lms.ccrp.dto.RewardHistoryDTO;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.service.RewardHistoryService;
import com.lms.ccrp.service.UserService;
import com.lms.ccrp.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private RewardHistoryService rewardHistoryService;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/dashboard")
    public ResponseEntity<AdminDashboardDTO> adminDashboard(@RequestHeader("Authorization") String token) {
        try {
            String authToken = token.replace("Bearer ", "");
            AdminDashboardDTO dto = userService.fetchAdminDashboard(authToken);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            throw new RuntimeException("Error in admin dashboard: " + e.getMessage());
        }
    }

    @PostMapping("/points")
    public ResponseEntity<String> adminTransactions(@RequestBody RewardHistoryDTO rewardHistoryDTO, @RequestHeader("Authorization") String authHeader) {
        try {
            long adminId = authUtil.authenticateAdminAndFetchId(authHeader);
            rewardHistoryDTO.setRequesterId(Role.ADMIN.name() + adminId);
            rewardHistoryService.performRewardTransactions(List.of(rewardHistoryDTO));
            return ResponseEntity.ok("Admin transaction for user processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/points/bulk-upload")
    public ResponseEntity<String> uploadAdminTransactions(@RequestParam("file") MultipartFile file, @RequestHeader("Authorization") String authHeader) {

        try {
            long adminId = authUtil.authenticateAdminAndFetchId(authHeader);
            List<RewardHistoryDTO> dtoList = rewardHistoryService.parseRTHFromExcelFile(file);
            rewardHistoryService.performRewardTransactions(dtoList);
            return ResponseEntity.ok("Admin transactions from Excel processed successfully.");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

}
