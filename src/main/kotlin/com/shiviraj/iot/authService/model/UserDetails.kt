package com.shiviraj.iot.authService.model

import com.shiviraj.iot.authService.controller.view.UserSignUpDetails
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
    val email: String,
    val password: String,
) {
    companion object {
        fun from(userId: String, userDetails: UserSignUpDetails, password: String): UserDetails {
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
