package com.lms.ccrp.dto;

import lombok.Data;

@Data
public class PasswordResetDTO {
    private String email;
    private String newPassword;
}