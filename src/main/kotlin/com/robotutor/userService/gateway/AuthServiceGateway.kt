package com.robotutor.userService.gateway

import com.robotutor.iot.service.WebClientWrapper
import com.robotutor.iot.utils.config.AppConfig
import com.robotutor.userService.config.AuthConfig
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class AuthServiceGateway(
    private val webClient: WebClientWrapper,
    private val authConfig: AuthConfig,
    private val appConfig: AppConfig
) {
    fun saveUserPassword(userId: String, password: String): Mono<Boolean> {
        return webClient.post(
            baseUrl = authConfig.baseUrl,
            path = authConfig.savePassword,
            body = mapOf("userId" to userId, "password" to password),
            headers = mapOf(
                HttpHeaders.AUTHORIZATION to appConfig.internalAccessToken,
                HttpHeaders.CONTENT_TYPE to "application/json"
            ),
            returnType = Boolean::class.java
        )
    }
}
