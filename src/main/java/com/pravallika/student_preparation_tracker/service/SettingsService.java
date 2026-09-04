package com.pravallika.student_preparation_tracker.service;

import com.pravallika.student_preparation_tracker.dto.SettingsRequest;
import com.pravallika.student_preparation_tracker.dto.SettingsResponse;
import com.pravallika.student_preparation_tracker.entity.User;
import com.pravallika.student_preparation_tracker.repository.UserRepository;

import org.springframework.stereotype.Service;

@Service
public class SettingsService {

    private final UserRepository userRepository;


    public SettingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // ==========================================
    // GET SETTINGS
    // ==========================================

    public SettingsResponse getSettings(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new RuntimeException(
                                "User not found"
                        )
                );

        return new SettingsResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.isDarkMode(),
                user.isEmailNotifications(),
                user.isTaskReminders()
        );
    }


    // ==========================================
    // UPDATE SETTINGS
    // ==========================================

    public SettingsResponse updateSettings(
            String currentEmail,
            SettingsRequest request) {

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(
                        () -> new RuntimeException(
                                "User not found"
                        )
                );


        // ------------------------------------------
        // Update name
        // ------------------------------------------

        if (request.getName() != null &&
                !request.getName().trim().isEmpty()) {

            user.setName(
                    request.getName().trim()
            );
        }


        // ------------------------------------------
        // Update email
        // ------------------------------------------

        if (request.getEmail() != null &&
                !request.getEmail().trim().isEmpty() &&
                !request.getEmail().equalsIgnoreCase(currentEmail)) {

            if (userRepository.existsByEmail(
                    request.getEmail().trim())) {

                throw new RuntimeException(
                        "Email already registered"
                );
            }

            user.setEmail(
                    request.getEmail().trim()
            );
        }


        // ------------------------------------------
        // Update settings
        // ------------------------------------------

        user.setDarkMode(
                request.isDarkMode()
        );

        user.setEmailNotifications(
                request.isEmailNotifications()
        );

        user.setTaskReminders(
                request.isTaskReminders()
        );


        // ------------------------------------------
        // Save
        // ------------------------------------------

        User savedUser =
                userRepository.save(user);


        return new SettingsResponse(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail(),
                savedUser.isDarkMode(),
                savedUser.isEmailNotifications(),
                savedUser.isTaskReminders()
        );
    }
}