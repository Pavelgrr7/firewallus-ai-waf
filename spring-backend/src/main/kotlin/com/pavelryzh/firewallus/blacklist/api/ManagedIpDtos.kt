package com.pavelryzh.firewallus.blacklist.api

import com.pavelryzh.firewallus.blacklist.domain.IpListType
import com.pavelryzh.firewallus.blacklist.domain.ManagedIp
import com.pavelryzh.firewallus.rule.domain.IpAddress
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.Instant
import java.util.*

data class CreateManagedIpDto(
    @field:NotBlank
    //TODO - Move regex validation to IpAddress type
    @field:Pattern(
        regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$|^([0-9a-fA-F]{0,4}:){2,7}[0-9a-fA-F]{0,4}$",
        message = "Неверный формат IP-адреса"
    )
    val ipAddress: IpAddress,

    val listType: IpListType,

    val description: String? = null
)


data class ManagedIpResponseDto(
    val id: UUID,
    val ipAddress: String,
    val description: String?,
    val timestamp: Instant?,
    val listType: IpListType,
)

fun ManagedIp.toDto(): ManagedIpResponseDto {
    return ManagedIpResponseDto(
        id = id!!,
        ipAddress = ipAddress,
        description = description,
        timestamp = timestamp,
        listType = listType
    )
}

fun List<ManagedIp>.toDtoList(): List<ManagedIpResponseDto> {
    return map { it.toDto() }
}