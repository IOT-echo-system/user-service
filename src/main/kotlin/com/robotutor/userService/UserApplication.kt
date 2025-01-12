package com.robotutor.userService

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(basePackages = ["com.robotutor"])
@ConfigurationPropertiesScan(basePackages = ["com.robotutor"])
@EnableCaching
class UserApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplicationBuilder(UserApplication::class.java).run(*args)
        }
    }
}

