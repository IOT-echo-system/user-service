package com.robotutor.userService.builder

import com.robotutor.userService.models.UserDetails
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class UserDetailsBuilder(
    val id: ObjectId? = null,
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val registeredAt: LocalDateTime = LocalDateTime.of(2024, 1, 1, 1, 1)
) {
    fun build(): UserDetails {
        return UserDetails(
            id = id,
            userId = userId,
            name = name,
            email = email,
            registeredAt = registeredAt
        )
    }
}
