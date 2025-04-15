package com.lms.ccrp.service;

import com.lms.ccrp.entity.JwtToken;
import com.lms.ccrp.entity.User;
import com.lms.ccrp.repository.JwtTokenRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtService {

    @Autowired
    private JwtTokenRepository jwtTokenRepository;

    @Value("${jwt.secret:MyJwtSecretKey12345678901234567890}") // Override in application.properties
    private String secret;

    private Key signingKey;

    private static final long jwtExpirationMinutes = 30;

    @PostConstruct
    public void init() {
        signingKey = Keys.hmacShaKeyFor(secret.getBytes());
    }

    @Transactional
    public String createToken(User user) {
        // Generate token using email, user ID, and role
        String token = createToken(user.getEmail(), user.getId(), user.getRole().name());

        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(jwtExpirationMinutes);
        jwtTokenRepository.deleteByUserId(user.getId());

        // Create and save the JWT token
        JwtToken jwtToken = new JwtToken();
        jwtToken.setToken(token);
        jwtToken.setUserId(user.getId());
        jwtToken.setExpiryDate(expiryDate);

        // Save the token in the database
        jwtTokenRepository.save(jwtToken);

        return token;
    }


    private String createToken(String username, Long userId, String role) {
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(username)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(jwtExpirationMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        final String username = extractUsername(token);
        return !isTokenExpired(token);
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Long extractUserId(String token) {
        return extractAllClaims(token).get("userId", Long.class);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        final Claims claims = extractAllClaims(token);
        return resolver.apply(claims);
    }
}
