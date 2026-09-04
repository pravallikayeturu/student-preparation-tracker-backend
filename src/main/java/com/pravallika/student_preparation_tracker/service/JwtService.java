package com.pravallika.student_preparation_tracker.service;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    // =====================================================
    // JWT SECRET KEY
    // =====================================================

    @Value("${JWT_SECRET_KEY}")
    private String secretKey;


    // =====================================================
    // TOKEN EXPIRATION
    // 4 HOURS
    // =====================================================

    private static final long EXPIRATION_TIME =
            1000L * 60 * 60 * 4;


    // =====================================================
    // CREATE SIGNING KEY
    // =====================================================

    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secretKey.getBytes(StandardCharsets.UTF_8)
        );
    }


    // =====================================================
    // GENERATE JWT TOKEN
    // =====================================================

    public String generateToken(String email) {

        return Jwts.builder()

                // Store email inside JWT subject
                .subject(email)

                // Token creation time
                .issuedAt(new Date())

                // Token expiration time
                .expiration(
                        new Date(
                                System.currentTimeMillis()
                                        + EXPIRATION_TIME
                        )
                )

                // Sign token
                .signWith(getSigningKey())

                .compact();
    }


    // =====================================================
    // EXTRACT EMAIL FROM TOKEN
    // =====================================================

    public String extractEmail(String token) {

        Claims claims =
                Jwts.parser()

                        .verifyWith(getSigningKey())

                        .build()

                        .parseSignedClaims(token)

                        .getPayload();

        return claims.getSubject();
    }


    // =====================================================
    // VALIDATE TOKEN
    // =====================================================

    public boolean isTokenValid(String token) {

        try {

            Claims claims =
                    Jwts.parser()

                            .verifyWith(getSigningKey())

                            .build()

                            .parseSignedClaims(token)

                            .getPayload();


            // Check subject/email exists
            String email =
                    claims.getSubject();


            return email != null
                    && !email.trim().isEmpty();


        } catch (Exception e) {

            System.out.println(
                    "JWT validation failed: "
                            + e.getMessage()
            );

            return false;
        }
    }
}