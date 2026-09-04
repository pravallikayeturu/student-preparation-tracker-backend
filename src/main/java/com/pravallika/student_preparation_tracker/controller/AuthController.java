package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.dto.AuthResponse;
import com.pravallika.student_preparation_tracker.dto.ForgotPasswordRequest;
import com.pravallika.student_preparation_tracker.dto.OtpLoginRequest;
import com.pravallika.student_preparation_tracker.dto.OtpSignupRequest;
import com.pravallika.student_preparation_tracker.dto.SignupRequest;
import com.pravallika.student_preparation_tracker.dto.ResetPasswordRequest;
import com.pravallika.student_preparation_tracker.dto.OtpVerificationRequest;
import com.pravallika.student_preparation_tracker.dto.UpdateSettingsRequest;
import com.pravallika.student_preparation_tracker.dto.ChangePasswordRequest;

import com.pravallika.student_preparation_tracker.service.AuthService;
import com.pravallika.student_preparation_tracker.service.OtpService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final OtpService otpService;

    public AuthController(
            AuthService authService,
            OtpService otpService) {

        this.authService = authService;
        this.otpService = otpService;
    }

    // =====================================================
    // SIGNUP
    // =====================================================

    @PostMapping("/signup")
    public AuthResponse signup(
            @Valid @RequestBody OtpSignupRequest request) {

        return authService.signup(request);
    }

    // =====================================================
    // LOGIN
    // =====================================================

    @PostMapping("/login")
    public AuthResponse login(
            @RequestBody OtpLoginRequest request) {

        return authService.login(request);
    }

    // =====================================================
    // SEND SIGNUP OTP
    // =====================================================

    @PostMapping("/send-otp")
    public String sendSignupOtp(
            @RequestBody SignupRequest request) {

        try {

            String otp =
                    otpService.generateSignupOtp(
                            request.getEmail()
                    );

            return "Signup OTP sent successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Unable to send Signup OTP. Please try again.";
        }
    }

    // =====================================================
    // SEND LOGIN OTP
    // =====================================================

    @PostMapping("/send-login-otp")
    public String sendLoginOtp(
            @RequestBody SignupRequest request) {

        try {

            String otp =
                    otpService.generateLoginOtp(
                            request.getEmail()
                    );

            return "Login OTP sent successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Unable to send Login OTP. Please try again.";
        }
    }

    // =====================================================
    // FORGOT PASSWORD
    // =====================================================

    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        try {

            String otp =
                    otpService.generateForgotPasswordOtp(
                            request.getEmail()
                    );

            return "Password reset OTP sent successfully";

        } catch (Exception e) {

            e.printStackTrace();

            return "Unable to send password reset OTP. Please try again.";
        }
    }

    // =====================================================
    // RESET PASSWORD
    // =====================================================

    @PostMapping("/reset-password")
    public AuthResponse resetPassword(
            @RequestBody ResetPasswordRequest request) {

        return authService.resetPassword(request);
    }

    // =====================================================
    // VERIFY OTP
    // =====================================================

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(
            @RequestBody OtpVerificationRequest request) {

        try {

            boolean verified =
                    otpService.verifyOtp(
                            request.getEmail(),
                            request.getOtp()
                    );

            if (!verified) {

                return ResponseEntity
                        .badRequest()
                        .body(
                                Map.of(
                                        "message",
                                        "Invalid or expired OTP"
                                )
                        );
            }

            return ResponseEntity
                    .ok(
                            Map.of(
                                    "message",
                                    "OTP verified successfully"
                            )
                    );

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .status(500)
                    .body(
                            Map.of(
                                    "message",
                                    "Something went wrong while verifying OTP. Please try again."
                            )
                    );
        }
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    @GetMapping("/profile")
    public ResponseEntity<AuthResponse> getProfile(
            Authentication authentication) {

        String email =
                authentication.getName();

        AuthResponse response =
                authService.getProfile(email);

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE NAME
    // =====================================================

    @PutMapping("/update-name")
    public ResponseEntity<AuthResponse> updateName(
            Authentication authentication,
            @RequestBody UpdateSettingsRequest request) {

        String email =
                authentication.getName();

        String name =
                request.getName();

        AuthResponse response =
                authService.updateName(
                        email,
                        name
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // UPDATE SETTINGS
    // =====================================================

    @PutMapping("/update-settings")
    public ResponseEntity<AuthResponse> updateSettings(
            Authentication authentication,
            @RequestBody UpdateSettingsRequest request) {

        String email =
                authentication.getName();

        AuthResponse response =
                authService.updateSettings(
                        email,
                        request
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            Authentication authentication,
            @RequestBody ChangePasswordRequest request) {

        try {

            String email =
                    authentication.getName();

            AuthResponse response =
                    authService.changePassword(
                            email,
                            request.getCurrentPassword(),
                            request.getNewPassword()
                    );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {

            e.printStackTrace();

            return ResponseEntity
                    .badRequest()
                    .body(
                            Map.of(
                                    "message",
                                    e.getMessage() != null
                                            ? e.getMessage()
                                            : "Something went wrong. Please try again later."
                            )
                    );
        }
    }
}