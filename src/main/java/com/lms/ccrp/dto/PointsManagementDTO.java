package com.lms.ccrp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PointsManagementDTO {
    private long customerId;
    private String name;
    private String email;
    private long points;
    List<RecentActivityDTO> recentActivity;
}
