package com.lms.ccrp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Date;

@Builder
@Data
public class RecentActivityDTO {
    private String requestType;
    private long pointsUsed;
    private String rewardDescription;
    private Date requestDate;
}
