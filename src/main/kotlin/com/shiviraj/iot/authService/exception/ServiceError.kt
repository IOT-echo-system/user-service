package com.shiviraj.iot.authService.exception

import com.shiviraj.iot.userService.exceptions.ServiceError


enum class IOTError(override val errorCode: String, override val message: String) : ServiceError {
    IOT0101("IOT-0101", "User already registered with this email."),
    IOT0102("IOT-0102", "Bad credentials."),
    IOT0103("IOT-0103", "Invalid token."),
}
