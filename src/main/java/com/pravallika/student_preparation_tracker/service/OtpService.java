package com.pravallika.student_preparation_tracker.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pravallika.student_preparation_tracker.entity.Otp;
import com.pravallika.student_preparation_tracker.repository.OtpRepository;

@Service
public class OtpService {

    private final OtpRepository otpRepository;
    private final BrevoEmailService brevoEmailService;

    public OtpService(
            OtpRepository otpRepository,
            BrevoEmailService brevoEmailService) {

        this.otpRepository = otpRepository;
        this.brevoEmailService = brevoEmailService;
    }

    // ==========================================
    // GENERATE OTP FOR SIGNUP
    // ==========================================

    public String generateSignupOtp(String email) {

        String otpCode = generateOtpCode();

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(new Otp());

        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(otp);

        // Signup-specific email
        brevoEmailService.sendSignupOtpEmail(
                email,
                otpCode
        );

        return otpCode;
    }

    // ==========================================
    // GENERATE OTP FOR LOGIN
    // ==========================================

    public String generateLoginOtp(String email) {

        String otpCode = generateOtpCode();

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(new Otp());

        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(otp);

        // Login-specific email
        brevoEmailService.sendLoginOtpEmail(
                email,
                otpCode
        );

        return otpCode;
    }

    // ==========================================
    // GENERATE OTP FOR FORGOT PASSWORD
    // ==========================================

    public String generateForgotPasswordOtp(String email) {

        String otpCode = generateOtpCode();

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(new Otp());

        otp.setEmail(email);
        otp.setOtp(otpCode);
        otp.setCreatedAt(LocalDateTime.now());
        otp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(otp);

        // Forgot-password-specific email
        brevoEmailService.sendForgotPasswordOtpEmail(
                email,
                otpCode
        );

        return otpCode;
    }

    // ==========================================
    // GENERATE OTP CODE
    // ==========================================

    private String generateOtpCode() {

        return String.format(
                "%06d",
                new Random().nextInt(1000000)
        );
    }

    // ==========================================
    // VERIFY OTP
    // ==========================================

    public boolean verifyOtp(
            String email,
            String otpCode) {

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(null);

        if (otp == null) {
            return false;
        }

        if (otp.getOtp() == null
                || !otp.getOtp().equals(otpCode)) {

            return false;
        }

        if (otp.getExpiresAt() == null
                || otp.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            otpRepository.delete(otp);

            return false;
        }

        // OTP is valid
        return true;
    }

    // ==========================================
    // DELETE OTP
    // ==========================================

    @Transactional
    public void deleteOtp(String email) {

        otpRepository.deleteByEmail(email);
    }
}