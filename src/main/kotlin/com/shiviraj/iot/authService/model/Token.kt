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
    val createdAt: LocalDateTime = LocalDateTime.now(),
    var expiredAt: LocalDateTime,
    val userId: UserId,
    val otpId: OtpId?
) {
    fun setExpired(): Token {
        this.expiredAt = LocalDateTime.now().minusDays(1)
        return this
    }

    companion object {
        fun generate(tokenId: String, userId: UserId, expiredAt: LocalDateTime, otpId: OtpId?): Token {
            return Token(
                tokenId = tokenId,
                userId = userId,
                value = generateTokenValue(),
                expiredAt = expiredAt,
                otpId = otpId
            )
        }

        private fun generateTokenValue(length: Int = 120): String {
            val chars = ('a'..'z') + ('A'..'Z') + ('0'..'9') + "`~!@#$%^&*()_+=-".split("")
            return List(length) { chars.random() }.joinToString("")
        }
    }
}


typealias TokenId = String
