package com.pavelryzh.plugins.di

import org.koin.core.definition.KoinDefinition
import org.koin.dsl.onClose
import org.slf4j.Logger

inline infix fun <reified T : AutoCloseable> KoinDefinition<T>.onCloseSafely(logger: Logger): KoinDefinition<T> {
    return this.onClose { bean ->
        val beanName = T::class.simpleName ?: "Service"

        runCatching {
            bean?.close()
        }.onSuccess {
            logger.info("$beanName closed successfully.")
        }.onFailure { e ->
            logger.error("Error closing $beanName: ${e.message}", e)
        }
    }
}