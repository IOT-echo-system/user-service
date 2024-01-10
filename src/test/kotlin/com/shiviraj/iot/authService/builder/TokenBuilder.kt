package com.shiviraj.iot.authService.builder

import com.shiviraj.iot.authService.model.Token
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class TokenBuilder(
    val id: ObjectId? = null,
    val tokenId: String = "tokenId",
    val value: String = "token value",
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiredAt: LocalDateTime = LocalDateTime.now(),
    val userId: String = "userID",
    val otpId: String? = "otpId"
) {
    fun build(): Token {
        return Token(
            id = id,
            tokenId = tokenId,
            value = value,
            createdAt = createdAt,
            expiredAt = expiredAt,
            userId = userId,
            otpId = otpId
        )
    }
}
