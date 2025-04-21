package com.lms.ccrp.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AdminDashboardDTO {
    private long totalPointsAwarded;
    private long activeUsers;
    List<PointsManagementDTO> allCustomersDTOList;
}
