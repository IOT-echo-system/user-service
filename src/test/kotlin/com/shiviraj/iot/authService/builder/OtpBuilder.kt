package com.shiviraj.iot.authService.builder

import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.OtpState
import org.bson.types.ObjectId
import java.time.LocalDateTime

data class OtpBuilder(
    val id: ObjectId? = null,
    val otpId: String = "",
    val value: String = "",
    val email: String = "",
    val userId: String = "",
    val state: OtpState = OtpState.GENERATED,
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    fun build(): Otp {
        return Otp(
            id = id,
            otpId = otpId,
            value = value,
            email = email,
            userId = userId,
            state = state,
            createdAt = createdAt
        )
    }
}
