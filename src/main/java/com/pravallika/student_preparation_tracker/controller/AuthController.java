package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.dto.AuthResponse;
import com.pravallika.student_preparation_tracker.dto.ForgotPasswordRequest;
import com.pravallika.student_preparation_tracker.dto.OtpLoginRequest;
import com.pravallika.student_preparation_tracker.dto.OtpSignupRequest;
import com.pravallika.student_preparation_tracker.dto.SignupRequest;
import com.pravallika.student_preparation_tracker.service.AuthService;
import com.pravallika.student_preparation_tracker.service.OtpService;

import org.springframework.web.bind.annotation.*;
import com.pravallika.student_preparation_tracker.dto.ResetPasswordRequest;
import com.pravallika.student_preparation_tracker.dto.OtpVerificationRequest;
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

    // =========================
    // SIGNUP WITH OTP
    // =========================
    @PostMapping("/signup")
    public AuthResponse signup(@RequestBody OtpSignupRequest request) {

        return authService.signup(request);
    }

    // =========================
    // LOGIN WITH OTP
    // =========================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody OtpLoginRequest request) {

        return authService.login(request);
    }

    // =========================
    // SEND OTP
    // =========================
    @PostMapping("/send-otp")
    public String sendOtp(@RequestBody SignupRequest request) {

        String otp = otpService.generateOtp(request.getEmail());

        return "OTP generated successfully: " + otp;
    }

    // =========================
    // FORGOT PASSWORD - SEND OTP
    // =========================
    @PostMapping("/forgot-password")
    public String forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        String otp = otpService.generateOtp(request.getEmail());

        return "Password reset OTP generated successfully: " + otp;
    }
    // =========================
// RESET PASSWORD
// =========================
@PostMapping("/reset-password")
public AuthResponse resetPassword(
        @RequestBody ResetPasswordRequest request) {

    return authService.resetPassword(request);
}


// =========================
// VERIFY OTP
// =========================
@PostMapping("/verify-otp")
public String verifyOtp(
        @RequestBody OtpVerificationRequest request) {

    boolean verified = otpService.verifyOtp(
            request.getEmail(),
            request.getOtp()
    );

    if (!verified) {
        throw new RuntimeException(
                "Invalid or expired OTP"
        );
    }

    return "OTP verified successfully";
}


}