package com.robotutor.authService.controllers.view

import com.robotutor.authService.models.UserDetails
import com.robotutor.authService.models.UserId
import com.robotutor.iot.utils.models.UserAuthenticationData
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserSignUpRequest(
    @field:NotBlank(message = "Name is required")
    @field:Size(min = 4, max = 30, message = "Name should not be less than 4 char or more than 30 char")
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
    val currentPassword: String? = null,
    @field:Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).+\$",
        message = "Password must contain at least one uppercase letter, one lowercase letter, and one digit"
    )
    val password: String
)


data class UpdateTokenRequest(
    @field:NotBlank(message = "ProjectId should not be blank!")
    val projectId: String,
    @field:NotBlank(message = "RoleId should not be blank")
    val roleId: String
)


data class UserSignUpResponse(val email: String, val userId: UserId, val name: String) {
    companion object {
        fun create(userDetails: UserDetails): UserSignUpResponse {
            return UserSignUpResponse(email = userDetails.email, userId = userDetails.userId, name = userDetails.name)
        }
    }
}

data class TokenResponse(val token: String, val success: Boolean)
data class TokenSecretKeyResponse(val secretKey: String)
data class LogoutResponse(val success: Boolean)
data class ResetPasswordResponse(val success: Boolean)

data class ValidateTokenResponse(val userId: UserId, val projectId: String, val roleId: String, val boardId: String?)

data class UserDetailsResponse(
    val userId: UserId,
    val name: String,
    val email: String,
    val registeredAt: LocalDateTime,
    val roleId: String
) {
    companion object {
        fun from(userDetails: UserDetails, authenticationData: UserAuthenticationData): UserDetailsResponse {
            return UserDetailsResponse(
                userId = userDetails.userId,
                name = userDetails.name,
                email = userDetails.email,
                registeredAt = userDetails.registeredAt,
                roleId = authenticationData.roleId
            )
        }
    }
}
