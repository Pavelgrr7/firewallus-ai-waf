package com.pavelryzh.firewallus.settings.port

import com.pavelryzh.firewallus.settings.domain.WafSettings
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface WafSettingsRepository: JpaRepository<WafSettings, Int> {
}