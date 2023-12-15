package com.shiviraj.iot.authService.builder

import com.shiviraj.iot.authService.model.UserDetails
import org.bson.types.ObjectId

data class UserDetailsBuilder(
    val id: ObjectId? = null,
    val userId: String = "",
    val name: String = "",
    val email: String = "",
    val password: String = ""
) {
    fun build(): UserDetails {
        return UserDetails(
            id = id,
            userId = userId,
            name = name,
            email = email,
            password = password
        )
    }
}
