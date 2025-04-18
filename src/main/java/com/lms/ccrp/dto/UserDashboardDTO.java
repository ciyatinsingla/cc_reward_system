package com.lms.ccrp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
public class UserDashboardDTO {
    private String name;
    private long points;
    List<RecentActivityDTO> recentActivity;
}
