package com.pravallika.student_preparation_tracker.filter;

import com.pravallika.student_preparation_tracker.service.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }


    // =====================================================
    // JWT FILTER
    // =====================================================

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {


        System.out.println(
                "JWT FILTER → "
                        + request.getMethod()
                        + " "
                        + request.getRequestURI()
        );


        // =================================================
        // GET AUTHORIZATION HEADER
        // =================================================

        String authHeader =
                request.getHeader("Authorization");


        if (authHeader == null
                || authHeader.trim().isEmpty()) {

            System.out.println(
                    "JWT FILTER → No Authorization header"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // CHECK BEARER TOKEN
        // =================================================

        if (!authHeader.startsWith("Bearer ")) {

            System.out.println(
                    "JWT FILTER → Invalid Authorization header"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // EXTRACT TOKEN
        // =================================================

        String token =
                authHeader.substring(7).trim();


        if (token.isEmpty()) {

            System.out.println(
                    "JWT FILTER → Empty JWT token"
            );

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }


        // =================================================
        // VALIDATE TOKEN
        // =================================================

        try {

            boolean valid =
                    jwtService.isTokenValid(token);


            if (!valid) {

                System.out.println(
                        "JWT FILTER → "
                                + "Token is invalid or expired"
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // EXTRACT EMAIL
            // =================================================

            String email =
                    jwtService.extractEmail(token);


            if (email == null
                    || email.trim().isEmpty()) {

                System.out.println(
                        "JWT FILTER → "
                                + "Email not found in token"
                );

                filterChain.doFilter(
                        request,
                        response
                );

                return;
            }


            // =================================================
            // AVOID REPLACING EXISTING AUTHENTICATION
            // =================================================

            if (SecurityContextHolder
                    .getContext()
                    .getAuthentication() == null) {


                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null,
                                AuthorityUtils.NO_AUTHORITIES
                        );


                authentication.setDetails(
                        new WebAuthenticationDetailsSource()
                                .buildDetails(request)
                );


                SecurityContextHolder
                        .getContext()
                        .setAuthentication(
                                authentication
                        );


                System.out.println(
                        "JWT authentication successful for: "
                                + email
                );

            }


        } catch (Exception e) {

            System.out.println(
                    "========== JWT ERROR =========="
            );

            System.out.println(
                    "Request: "
                            + request.getRequestURI()
            );

            System.out.println(
                    "Error: "
                            + e.getMessage()
            );

            System.out.println(
                    "========== JWT ERROR END =========="
            );
        }


        // =================================================
        // CONTINUE REQUEST
        // =================================================

        filterChain.doFilter(
                request,
                response
        );
    }
}