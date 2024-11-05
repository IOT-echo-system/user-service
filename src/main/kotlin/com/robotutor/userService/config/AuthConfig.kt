package com.robotutor.userService.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth")
data class AuthConfig(val baseUrl: String, val savePassword: String)
