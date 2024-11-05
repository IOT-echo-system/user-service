package com.robotutor.userService.exceptions

import com.robotutor.iot.exceptions.ServiceError


enum class IOTError(override val errorCode: String, override val message: String) : ServiceError {
    IOT0201("IOT-0201", "User already registered with this email."),
    IOT0206("IOT-0202", "User not found with this email."),
}
