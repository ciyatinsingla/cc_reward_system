package com.lms.ccrp.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "jwt_tokens")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String token;
    private Long userId;
    private LocalDateTime expiryDate;
}