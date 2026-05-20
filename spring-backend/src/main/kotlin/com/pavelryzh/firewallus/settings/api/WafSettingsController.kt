package com.pavelryzh.firewallus.settings.api

import com.pavelryzh.firewallus.settings.service.WafSettingsService
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/settings")
@PreAuthorize("hasRole('ADMIN')")
class WafSettingsController(private val wafSettingsService: WafSettingsService) {

    @GetMapping
    fun getSettings(): SettingsResponseDto {
        return wafSettingsService.getSettings().toDto()
    }

    @PatchMapping
    fun updateSettings(@Valid @RequestBody settingsDto: UpdateSettingsDto): SettingsResponseDto {
        return wafSettingsService.updateSettings(settingsDto).toDto()
    }

}