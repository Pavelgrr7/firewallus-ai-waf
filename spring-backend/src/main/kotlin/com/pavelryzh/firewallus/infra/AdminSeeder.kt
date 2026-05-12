package com.pavelryzh.firewallus.infra

import com.pavelryzh.firewallus.user.AdminRepository
import com.pavelryzh.firewallus.user.domain.Admin

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class AdminSeeder(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value($$"${app.default-admin.username}") private val defaultUsername: String,
    @Value($$"${app.default-admin.password}") private val defaultPassword: String
) : ApplicationRunner {

    private val logger = LoggerFactory.getLogger(AdminSeeder::class.java)

    override fun run(args: ApplicationArguments) {
        if (adminRepository.count() == 0L) {
            logger.info("База данных админов пуста. Создаем дефолтного администратора: $defaultUsername")

            val defaultAdmin = Admin(
                username = defaultUsername,
                passwordHash = passwordEncoder.encode(defaultPassword)!!
            )

            adminRepository.save(defaultAdmin)
            logger.info("Дефолтный администратор успешно создан.")
        } else {
            logger.debug("Администраторы уже существуют, Seeding пропущен.")
        }
    }
}