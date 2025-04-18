package com.lms.ccrp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
public class RecentActivityDTO {
    private String requestType;
    private long pointsUsed;
    private String rewardDescription;
    private Date requestDate;
}
