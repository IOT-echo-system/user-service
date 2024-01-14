package com.shiviraj.iot.authService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.shiviraj.iot"])
@ConfigurationPropertiesScan(basePackages = ["com.shiviraj.iot"])
class AuthServiceApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(AuthServiceApplication::class.java).run(*args)
        }
    }
}

