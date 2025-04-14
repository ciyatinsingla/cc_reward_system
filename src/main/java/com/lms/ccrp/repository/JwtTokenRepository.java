package com.lms.ccrp.repository;

import com.lms.ccrp.entity.JwtToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface JwtTokenRepository extends JpaRepository<JwtToken, Long> {

    Optional<JwtToken> findByToken(String token);

    void deleteByUserId(Long userId);

    void deleteByExpiryDateBefore(LocalDateTime expiryDate);
}