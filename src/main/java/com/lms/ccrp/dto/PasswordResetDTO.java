package com.lms.ccrp.dto;

import lombok.Data;
import lombok.NonNull;

@Data
public class PasswordResetDTO {
    @NonNull
    private String email;
    private String newPassword;
    private String otp;
}