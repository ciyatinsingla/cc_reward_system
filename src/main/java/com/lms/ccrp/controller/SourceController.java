package com.lms.ccrp.controller;

import com.lms.ccrp.entity.RewardHistory;
import com.lms.ccrp.service.RewardHistoryService;
import com.lms.ccrp.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/source")
public class SourceController {

    @Autowired
    private RewardHistoryService service;

    @Autowired
    private AuthUtil authUtil;

    @GetMapping("/begin")
    public String beginSync(@RequestHeader("Authorization") String authHeader) throws Exception {
        authUtil.authenticateAdminAndFetchId(authHeader);
        List<RewardHistory> sourceRecords = service.parseSRTExcelFile();
        if(CollectionUtils.isEmpty(sourceRecords))
            return "File is empty, please reach out to the source.";
        return service.requestTransactions(sourceRecords);
    }

    @GetMapping("/end")
    public void endSync(@RequestHeader("Authorization") String authHeader) throws Exception {
        authUtil.authenticateAdminAndFetchId(authHeader);
        service.finalizeSyncHistory();
    }

}
