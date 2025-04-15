package com.lms.ccrp.dto;

import com.lms.ccrp.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private Role role;
}
