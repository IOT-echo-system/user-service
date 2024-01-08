package com.shiviraj.iot.authService.model

import com.shiviraj.iot.authService.controller.view.UserSignUpRequest
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

const val USER_COLLECTION = "users"

@TypeAlias("User")
@Document(USER_COLLECTION)
data class UserDetails(
    @Id
    var id: ObjectId? = null,
    @Indexed(unique = true)
    val userId: UserId,
    val name: String,
    @Indexed(unique = true)
    val email: String,
    var password: String,
) {
    fun updatePassword(encodedPassword: String): UserDetails {
        this.password = encodedPassword
        return this
    }

    companion object {
        fun from(userId: String, userDetails: UserSignUpRequest, password: String): UserDetails {
            return UserDetails(
                userId = userId,
                name = userDetails.name,
                email = userDetails.email,
                password = password
            )
        }
    }
}

typealias UserId = String
