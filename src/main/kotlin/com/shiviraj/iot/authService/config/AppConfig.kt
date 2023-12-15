package com.shiviraj.iot.authService.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.env")
data class AppConfig(val secretKey: String)
