package com.shiviraj.iot.authService.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime


const val TOKEN_COLLECTION = "tokens"

@TypeAlias("Token")
@Document(TOKEN_COLLECTION)
data class Token(
    @Id
    var id: ObjectId? = null,
    @Indexed(unique = true)
    val tokenId: TokenId,
    val value: String,
    @Indexed(name = "sessionExpiryIndex", expireAfterSeconds = 604800)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun from(tokenId: String, value: String): Token {
            return Token(tokenId = tokenId, value = value)
        }
    }
}

typealias TokenId = String
