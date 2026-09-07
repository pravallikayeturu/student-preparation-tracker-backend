package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.dto.AuthResponse;
import com.pravallika.student_preparation_tracker.dto.OtpLoginRequest;
import com.pravallika.student_preparation_tracker.dto.OtpSignupRequest;
import com.pravallika.student_preparation_tracker.dto.ResetPasswordRequest;
import com.pravallika.student_preparation_tracker.dto.UpdateSettingsRequest;
import com.pravallika.student_preparation_tracker.entity.User;
import com.pravallika.student_preparation_tracker.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service public class AuthService {

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

    // =====================================================
    // SIGNUP
    // =====================================================

    public AuthResponse signup(OtpSignupRequest request) {

        try {

            boolean verified = otpService.verifyOtp(
                    request.getEmail(),
                    request.getOtp()
            );

            if (!verified) {
                throw new RuntimeException(
                        "Invalid or expired OTP"
                );
            }

            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new RuntimeException(
                        "Email already registered"
                );
            }

            User user = new User();

            user.setName(
                    request.getName() == null
                            || request.getName().trim().isEmpty()
                            ? "User"
                            : request.getName().trim()
            );

            user.setEmail(request.getEmail());

            user.setPassword(
                    passwordEncoder.encode(
                            request.getPassword()
                    )
            );

            user.setDarkMode(false);
            user.setEmailNotifications(true);
            user.setTaskReminders(true);

            User savedUser =
                    userRepository.save(user);

            otpService.deleteOtp(
                    request.getEmail()
            );

            return new AuthResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "Signup successful"
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Signup failed. Please try again."
            );
        }
    }

    // =====================================================
    // LOGIN
    // =====================================================

    public AuthResponse login(OtpLoginRequest request) {

        try {

            boolean verified = otpService.verifyOtp(
                    request.getEmail(),
                    request.getOtp()
            );

            if (!verified) {
                throw new RuntimeException(
                        "Invalid or expired OTP"
                );
            }

            User user =
                    userRepository.findByEmail(
                            request.getEmail()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "User not registered. Please sign up frist."
                            )
                    );

            if (!passwordEncoder.matches(
                    request.getPassword(),
                    user.getPassword())) {

                throw new RuntimeException(
                        "Invalid password"
                );
            }

            String token =
                    jwtService.generateToken(
                            user.getEmail()
                    );

            otpService.deleteOtp(
                    request.getEmail()
            );

            return new AuthResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    "Login successful",
                    token
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Login failed. Please try again."
            );
        }
    }

        // =====================================================
        // VERIFY LOGIN PASSWORD BEFORE SENDING OTP
        // =====================================================

        public void verifyLoginPassword(
        String email,
        String password) {

    User user =
            userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Please signup first , then login"
                            )
                    );

    if (!passwordEncoder.matches(
            password,
            user.getPassword())) {

        throw new RuntimeException(
                "Incorrect password"
        );
    }
}


    // =====================================================
    // RESET PASSWORD
    // =====================================================

    public AuthResponse resetPassword(
            ResetPasswordRequest request) {

        try {

            boolean verified = otpService.verifyOtp(
                    request.getEmail(),
                    request.getOtp()
            );

            if (!verified) {
                throw new RuntimeException(
                        "Invalid or expired OTP"
                );
            }

            User user =
                    userRepository.findByEmail(
                            request.getEmail()
                    ).orElseThrow(() ->
                            new RuntimeException(
                                    "User not found"
                            )
                    );

            if (request.getNewPassword() == null
                    || request.getNewPassword().length() < 6) {

                throw new RuntimeException(
                        "Password must contain at least 6 characters"
                );
            }

            user.setPassword(
                    passwordEncoder.encode(
                            request.getNewPassword()
                    )
            );

            userRepository.save(user);

            otpService.deleteOtp(
                    request.getEmail()
            );

            return new AuthResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    "Password reset successfully"
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to reset password. Please try again."
            );
        }
    }

    // =====================================================
    // CHANGE PASSWORD
    // =====================================================

    public AuthResponse changePassword(
            String email,
            String currentPassword,
            String newPassword) {

        try {

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            if (!passwordEncoder.matches(
                    currentPassword,
                    user.getPassword())) {

                throw new RuntimeException(
                        "Current password is incorrect"
                );
            }

            if (newPassword == null
                    || newPassword.length() < 6) {

                throw new RuntimeException(
                        "New password must contain at least 6 characters"
                );
            }

            if (passwordEncoder.matches(
                    newPassword,
                    user.getPassword())) {

                throw new RuntimeException(
                        "New password must be different from current password"
                );
            }

            user.setPassword(
                    passwordEncoder.encode(
                            newPassword
                    )
            );

            User savedUser =
                    userRepository.save(user);

            return new AuthResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "Password changed successfully"
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to change password. Please try again."
            );
        }
    }

    // =====================================================
    // GET PROFILE
    // =====================================================

    public AuthResponse getProfile(String email) {

        try {

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            return new AuthResponse(
                    user.getId(),
                    user.getName(),
                    user.getEmail(),
                    "Profile loaded successfully",
                    user.isDarkMode(),
                    user.isEmailNotifications(),
                    user.isTaskReminders()
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to load profile"
            );
        }
    }

    // =====================================================
    // UPDATE NAME
    // =====================================================

    public AuthResponse updateName(
            String email,
            String newName) {

        try {

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            if (newName == null
                    || newName.trim().isEmpty()) {

                throw new RuntimeException(
                        "Name cannot be empty"
                );
            }

            user.setName(
                    newName.trim()
            );

            User savedUser =
                    userRepository.save(user);

            return new AuthResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "Name updated successfully"
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to update name. Please try again."
            );
        }
    }

    // =====================================================
    // UPDATE ALL SETTINGS
    // =====================================================

    public AuthResponse updateSettings(
            String email,
            UpdateSettingsRequest request) {

        try {

            User user =
                    userRepository.findByEmail(email)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "User not found"
                                    )
                            );

            if (request.getDarkMode() != null) {

                user.setDarkMode(
                        request.getDarkMode()
                );
            }

            if (request.getEmailNotifications() != null) {

                user.setEmailNotifications(
                        request.getEmailNotifications()
                );
            }

            if (request.getTaskReminders() != null) {

                user.setTaskReminders(
                        request.getTaskReminders()
                );
            }

            User savedUser =
                    userRepository.save(user);

            return new AuthResponse(
                    savedUser.getId(),
                    savedUser.getName(),
                    savedUser.getEmail(),
                    "Settings updated successfully",
                    savedUser.isDarkMode(),
                    savedUser.isEmailNotifications(),
                    savedUser.isTaskReminders()
            );

        } catch (RuntimeException e) {

            throw e;

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to update settings. Please try again."
            );
        }
    }
}