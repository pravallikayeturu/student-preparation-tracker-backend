package com.pravallika.student_preparation_tracker.controller;

import com.pravallika.student_preparation_tracker.dto.SettingsRequest;
import com.pravallika.student_preparation_tracker.dto.SettingsResponse;
import com.pravallika.student_preparation_tracker.service.SettingsService;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    private final SettingsService settingsService;


    public SettingsController(
            SettingsService settingsService) {

        this.settingsService = settingsService;
    }


    // ==========================================
    // GET SETTINGS
    // ==========================================

    @GetMapping
    public SettingsResponse getSettings(
            Authentication authentication) {

        String email =
                authentication.getName();

        return settingsService.getSettings(email);
    }


    // ==========================================
    // UPDATE SETTINGS
    // ==========================================

    @PutMapping
    public SettingsResponse updateSettings(
            @RequestBody SettingsRequest request,
            Authentication authentication) {

        String currentEmail =
                authentication.getName();

        return settingsService.updateSettings(
                currentEmail,
                request
        );
    }
}