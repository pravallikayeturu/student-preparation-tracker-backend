package com.pravallika.student_preparation_tracker.config;

import com.pravallika.student_preparation_tracker.filter.JwtAuthenticationFilter;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter) {

        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }


    // =====================================================
    // SECURITY FILTER CHAIN
    // =====================================================

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http

            // -------------------------------------------------
            // CORS
            // -------------------------------------------------

            .cors(cors -> {})


            // -------------------------------------------------
            // CSRF
            // -------------------------------------------------

            .csrf(csrf -> csrf.disable())


            // -------------------------------------------------
            // SESSION
            // -------------------------------------------------

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )


            // -------------------------------------------------
            // AUTHORIZATION
            // -------------------------------------------------

            .authorizeHttpRequests(auth -> auth

                // =================================================
                // CORS PREFLIGHT
                // =================================================

                .requestMatchers(
                    HttpMethod.OPTIONS,
                    "/**"
                ).permitAll()


                // =================================================
                // AUTHENTICATION ENDPOINTS
                // =================================================

                .requestMatchers(
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/send-otp",
                    "/api/auth/send-login-otp",
                    "/api/auth/verify-otp",
                    "/api/auth/forgot-password",
                    "/api/auth/reset-password"
                ).permitAll()


                // =================================================
                // AI CHATBOT
                // =================================================

                .requestMatchers(
                    "/api/chatbot/**"
                ).authenticated()


                // =================================================
                // STUDY TASKS
                // =================================================

                .requestMatchers(
                    "/api/tasks/**"
                ).authenticated()

                    // =================================================
// SETTINGS
// =================================================

.requestMatchers(
    "/api/settings/**"
).authenticated()
                // =================================================
                // FILES
                // =================================================

                .requestMatchers(
                    "/api/files/**"
                ).authenticated()


                // =================================================
                // EVERYTHING ELSE
                // =================================================

                .anyRequest().authenticated()
            )


            // -------------------------------------------------
            // JWT FILTER
            // -------------------------------------------------

            .addFilterBefore(
                jwtAuthenticationFilter,
                UsernamePasswordAuthenticationFilter.class
            );


        return http.build();
    }


    // =====================================================
    // CORS CONFIGURATION
    // =====================================================

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration =
                new CorsConfiguration();


        configuration.setAllowedOrigins(
            List.of(
                        
                        "http://localhost:5173",
        "https://student-preparation-tracker-fronten.vercel.app"

            )
        );


        configuration.setAllowedMethods(
            List.of(
                "GET",
                "POST",
                "PUT",
                "DELETE",
                "PATCH",
                "OPTIONS"
            )
        );


        configuration.setAllowedHeaders(
            List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin"
            )
        );


        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();


        source.registerCorsConfiguration(
            "/**",
            configuration
        );


        return source;
    }


    // =====================================================
    // PASSWORD ENCODER
    // =====================================================

    @Bean
    public PasswordEncoder passwordEncoder() {

        return new BCryptPasswordEncoder();
    }
}