package com.silaipro.controller;

import com.silaipro.security.HasPermission;
import com.silaipro.service.ShopSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
@Tag(name = "Settings", description = "Shop configuration and settings")
public class ShopSettingController {

    private final ShopSettingService shopSettingService;

    @GetMapping
    @HasPermission("ADMIN")
    @Operation(summary = "Get all shop settings (Admin Only)")
    public Map<String, String> getAllSettings() {
        return shopSettingService.getAllSettings();
    }

    @GetMapping("/public")
    @Operation(summary = "Get public shop settings (No auth)")
    public Map<String, String> getPublicSettings() {
        return shopSettingService.getPublicSettings();
    }

    @PutMapping
    @HasPermission("ADMIN")
    @Operation(summary = "Bulk update shop settings (Admin Only)")
    public void updateSettings(@RequestBody Map<String, String> settings) {
        shopSettingService.updateSettings(settings);
    }

    @PutMapping("/{key}")
    @HasPermission("ADMIN")
    @Operation(summary = "Update a single shop setting (Admin Only)")
    public void updateSetting(@PathVariable String key, @RequestBody String value) {
        shopSettingService.updateSetting(key, value);
    }
}
