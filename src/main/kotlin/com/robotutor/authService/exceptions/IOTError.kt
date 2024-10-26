package com.robotutor.authService.exceptions

import com.robotutor.iot.exceptions.ServiceError


enum class IOTError(override val errorCode: String, override val message: String) : ServiceError {
    IOT0101("IOT-0101", "User already registered with this email."),
    IOT0102("IOT-0102", "Bad credentials."),
    IOT0103("IOT-0103", "Invalid token."),
    IOT0104("IOT-0104", "Otp generation limit exceed."),
    IOT0105("IOT-0105", "Invalid otp."),
    IOT0106("IOT-0106", "User not registered with this email."),
}
