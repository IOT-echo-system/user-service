package com.shiviraj.iot.authService.repository

import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.OtpId
import com.shiviraj.iot.authService.model.OtpState
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface OtpRepository : ReactiveCrudRepository<Otp, OtpId> {
    fun findByEmailAndState(email: String, state: OtpState): Mono<Otp>
    fun countByEmailAndCreatedAtAfter(email: String, createdAt: LocalDateTime): Mono<Long>
    fun findByOtpIdAndState(otpId: OtpId, state: OtpState): Mono<Otp>
    fun findAllByStateAndCreatedAtBefore(state: OtpState, createdAt: LocalDateTime): Flux<Otp>
}
