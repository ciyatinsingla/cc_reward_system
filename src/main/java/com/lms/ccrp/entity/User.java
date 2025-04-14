package com.lms.ccrp.entity;

import com.lms.ccrp.entity.common.Auditable;
import com.lms.ccrp.enums.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String userName;

    private String email;

    private String password;

    private Date dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Role role;
}


