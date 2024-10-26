package com.robotutor.authService.controllers.view

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class GenerateOtpRequest(
    @field:Email(message = "Email should be valid.")
    val email: String
)

data class VerifyOtpRequest(
    @field:NotBlank(message = "OtpId must not be blank.")
    val otpId: String,
    @field:NotBlank(message = "Otp must not be blank.")
    val otp: String
)

data class OtpResponse(val otpId: String, val success: Boolean, val generateAt: LocalDateTime)
