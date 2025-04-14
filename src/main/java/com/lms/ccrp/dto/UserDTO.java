package com.lms.ccrp.dto;

import com.lms.ccrp.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private String userName;
    private String email;
    private String password;
    private Role role;
}
