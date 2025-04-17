package com.lms.ccrp.controller;

import com.lms.ccrp.entity.RequestTransactions;
import com.lms.ccrp.enums.Role;
import com.lms.ccrp.service.JwtService;
import com.lms.ccrp.service.RewardTransactionService;
import com.lms.ccrp.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/source-validator")
public class RequestController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserService userService;

    @Autowired
    private RewardTransactionService service;

    @Value("${source.file.path}")
    private String excelFile;


    @PostMapping("/transactions")
    public void register(@RequestHeader("Authorization") String authHeader) throws Exception {
        Long userId = jwtService.authenticateAndExtractUserId(authHeader);
        if (userService.fetchUser(userId).getRole() != Role.ADMIN)
            throw new RuntimeException("Action not allowed");

        List<RequestTransactions> sourceRecords = service.parseSRTFromExcelFile(excelFile);
        service.requestTransactions(sourceRecords);
    }

}
