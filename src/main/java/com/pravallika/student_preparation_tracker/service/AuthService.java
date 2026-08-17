package com.pravallika.student_preparation_tracker.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.pravallika.student_preparation_tracker.dto.AuthResponse;
import com.pravallika.student_preparation_tracker.dto.OtpLoginRequest;
import com.pravallika.student_preparation_tracker.dto.OtpSignupRequest;
import com.pravallika.student_preparation_tracker.dto.ResetPasswordRequest;
import com.pravallika.student_preparation_tracker.entity.User;
import com.pravallika.student_preparation_tracker.repository.UserRepository;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OtpService otpService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            OtpService otpService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    // =========================
    // SIGNUP WITH OTP
    // =========================
    public AuthResponse signup(OtpSignupRequest request) {

        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        request.getPassword()
                )
        );

        User savedUser = userRepository.save(user);

        // Delete OTP only after successful signup
        otpService.deleteOtp(request.getEmail());

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                "Signup successful"
        );
    }

    // =========================
    // LOGIN WITH OTP
    // =========================
    public AuthResponse login(OtpLoginRequest request) {

        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (!verified) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword())) {

            throw new RuntimeException("Invalid password");
        }

        // Generate JWT
        String token = jwtService.generateToken(
                user.getEmail()
        );

        // Delete OTP only after successful login
        otpService.deleteOtp(request.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Login successful",
                token
        );
    }

    // =========================
    // RESET PASSWORD
    // =========================
    public AuthResponse resetPassword(
            ResetPasswordRequest request) {

        // Check that OTP was verified
        boolean verified = otpService.verifyOtp(
                request.getEmail(),
                request.getOtp()
        );

        if (!verified) {
            throw new RuntimeException(
                    "Invalid or expired OTP"
            );
        }

        // Find user
        User user = userRepository.findByEmail(
                request.getEmail()
        ).orElseThrow(
                () -> new RuntimeException("User not found")
        );

        // Encrypt new password
        user.setPassword(
                passwordEncoder.encode(
                        request.getNewPassword()
                )
        );

        // Save new password
        userRepository.save(user);

        // Delete OTP only after password is successfully changed
        otpService.deleteOtp(request.getEmail());

        return new AuthResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                "Password reset successfully"
        );
    }
}