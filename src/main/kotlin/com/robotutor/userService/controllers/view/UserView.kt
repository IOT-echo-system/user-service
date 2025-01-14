package com.robotutor.userService.controllers.view

import com.robotutor.userService.models.UserDetails
import com.robotutor.userService.models.UserId
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class UserRegistrationRequest(
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


data class UserDetailsView(
    val userId: UserId,
    val name: String,
    val email: String,
    val registeredAt: LocalDateTime,
) {
    companion object {
        fun from(userDetails: UserDetails): UserDetailsView {
            return UserDetailsView(
                userId = userDetails.userId,
                name = userDetails.name,
                email = userDetails.email,
                registeredAt = userDetails.registeredAt,
            )
        }
    }
}
