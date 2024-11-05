package com.robotutor.userService.gateway

import com.robotutor.userService.config.AuthConfig
import com.robotutor.iot.service.WebClientWrapper
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthServiceGateway(private val webClient: WebClientWrapper, private val authConfig: AuthConfig) {
    fun saveUserPassword(userId: String, password: String): Mono<Boolean> {
        return webClient.post(
            baseUrl = authConfig.baseUrl,
            path = authConfig.savePassword,
            body = mapOf("userId" to userId, "password" to password),
            returnType = Boolean::class.java
        )
    }
}
