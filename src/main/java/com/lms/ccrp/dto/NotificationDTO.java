package com.lms.ccrp.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationDTO {
    private String recipientTo;
    private String username;
    private long points;
    private String reason;
}
