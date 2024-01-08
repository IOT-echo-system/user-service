package com.shiviraj.iot.authService.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.TypeAlias
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

const val OTP_COLLECTION = "otps"

@TypeAlias("Otp")
@Document(OTP_COLLECTION)
data class Otp(
    @Id
    var id: ObjectId? = null,
    @Indexed(unique = true)
    val otpId: TokenId,
    val value: String,
    val email: String,
    var state: OtpState = OtpState.GENERATED,
    @Indexed(name = "sessionExpiryIndex", expireAfterSeconds = 300)
    val createdAt: LocalDateTime = LocalDateTime.now(),
) {
    fun setExpired(): Otp {
        this.state = OtpState.EXPIRED
        return this
    }

    fun isValidOtp(otp: String): Boolean = this.value == otp

    companion object {
        fun create(otpId: String, email: String): Otp {
            return Otp(otpId = otpId, value = generateOTP(6), email = email, state = OtpState.GENERATED)
        }
    }


}

fun generateOTP(length: Int): String {
    val chars = ('0'..'9').toList()
    return List(length) { chars[(0..9).random()] }.joinToString("")
}

enum class OtpState {
    GENERATED,
    VERIFIED,
    EXPIRED
}

typealias OtpId = String
