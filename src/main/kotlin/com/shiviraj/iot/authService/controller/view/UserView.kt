package com.shiviraj.iot.authService.controller.view

import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.model.UserId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class UserSignUpRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,
    @field:Email(message = "Email should be valid")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+\$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    val password: String
)

data class UserLoginRequest(
    @field:Email(message = "Email should be valid")
    val email: String,
    @field:NotBlank(message = "Password is required")
    val password: String
)

data class ResetPasswordRequest(
    val currentPassword: String?,
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+\$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    val password: String
)


data class UserSignUpResponse(val email: String, val userId: UserId, val name: String) {
    companion object {
        fun create(userDetails: UserDetails): UserSignUpResponse {
            return UserSignUpResponse(email = userDetails.email, userId = userDetails.userId, name = userDetails.name)
        }
    }
}

data class TokenResponse(val token: String, val success: Boolean)
data class ResetPasswordResponse(val success: Boolean)

data class ValidateTokenResponse(val userId: UserId)

