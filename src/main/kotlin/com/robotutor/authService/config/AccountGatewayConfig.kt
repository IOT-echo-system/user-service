package com.robotutor.authService.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.auth.account-gateway")
data class AccountGatewayConfig(val baseUrl: String, val validateRoleAndAccountPath: String)
