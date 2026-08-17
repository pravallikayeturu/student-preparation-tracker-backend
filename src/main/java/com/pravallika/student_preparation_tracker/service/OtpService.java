package com.pravallika.student_preparation_tracker.service;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;

import com.pravallika.student_preparation_tracker.entity.Otp;
import com.pravallika.student_preparation_tracker.repository.OtpRepository;

import org.springframework.transaction.annotation.Transactional;
@Service
public class OtpService {

    private final OtpRepository otpRepository;

    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    // =========================
    // GENERATE OTP
    // =========================
    public String generateOtp(String email) {

        String otpCode = String.format(
                "%06d",
                new Random().nextInt(1000000)
        );

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(new Otp());

        otp.setEmail(email);
        otp.setOtp(otpCode);

        // OTP valid for 5 minutes
        otp.setCreatedAt(LocalDateTime.now());

        otp.setExpiresAt(
                LocalDateTime.now().plusMinutes(5)
        );

        otpRepository.save(otp);

        return otpCode;
    }

    // =========================
    // VERIFY OTP
    // =========================
    // This method checks the OTP
    // but does NOT delete it.
    // =========================
    public boolean verifyOtp(
            String email,
            String otpCode) {

        Otp otp = otpRepository
                .findByEmail(email)
                .orElse(null);

        if (otp == null) {
            return false;
        }

        // Check OTP value
        if (!otp.getOtp().equals(otpCode)) {
            return false;
        }

        // Check OTP expiry
        if (otp.getExpiresAt() == null
                || otp.getExpiresAt()
                        .isBefore(LocalDateTime.now())) {

            // Delete expired OTP
            otpRepository.delete(otp);

            return false;
        }

        return true;
    }

    // =========================
    // DELETE OTP
    // =========================
    @Transactional
    public void deleteOtp(String email) {

        otpRepository.deleteByEmail(email);
    }
}