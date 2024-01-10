package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.controller.view.GenerateOtpRequest
import com.shiviraj.iot.authService.controller.view.VerifyOtpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.OtpState
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.repository.OtpRepository
import com.shiviraj.iot.loggingstarter.logOnError
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.TooManyRequestsException
import com.shiviraj.iot.utils.service.IdGeneratorService
import com.shiviraj.iot.utils.utils.createMono
import com.shiviraj.iot.utils.utils.createMonoError
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.time.LocalDateTime

@Service
class OtpService(
    private val idGeneratorService: IdGeneratorService,
    private val otpRepository: OtpRepository,
    private val tokenService: TokenService,
    private val userService: UserService
) {
    fun generateOtp(generateOtpRequest: GenerateOtpRequest): Mono<Otp> {
        return otpRepository.countByEmailAndCreatedAtAfter(
            generateOtpRequest.email,
            LocalDateTime.now().minusMinutes(10)
        )
            .flatMap { count ->
                if (count >= 3) {
                    createMonoError<Otp>(TooManyRequestsException(IOTError.IOT0104))
                        .logOnError(errorMessage = "Too many request for otp generation")
                } else {
                    otpRepository.findByEmailAndState(generateOtpRequest.email, OtpState.GENERATED)
                }
            }
            .flatMap {
                otpRepository.save(it.setExpired())
                    .logOnSuccess(message = "Set otp as expired")
                    .logOnError(errorMessage = "Failed to set otp as expired")
            }
            .switchIfEmpty {
                createMono(Otp(otpId = "vidisse", value = "nobis", email = "a@email.com", userId = "userId"))
            }
            .flatMap { userService.getUserByEmail(generateOtpRequest.email) }
            .flatMap { userDetails ->
                idGeneratorService.generateId(IdType.OTP_ID)
                    .flatMap { otpId ->
                        otpRepository.save(Otp.create(otpId, userDetails))
                    }
            }
            .logOnSuccess(message = "Successfully generated otp")
            .logOnError(errorMessage = "Failed to generate otp")
    }

    fun verifyOtp(verifyOtpRequest: VerifyOtpRequest): Mono<Token> {
        return otpRepository.findByOtpIdAndState(verifyOtpRequest.otpId, OtpState.GENERATED)
            .flatMap {
                if (it.isValidOtp(verifyOtpRequest.otp)) {
                    otpRepository.save(it.setVerified())
                } else {
                    createMonoError(BadDataException(IOTError.IOT0105))
                }
            }
            .logOnSuccess(message = "Successfully verified otp")
            .logOnError(errorMessage = "Failed to verify otp")
            .flatMap {
                tokenService.generateToken(
                    userId = it.userId,
                    expiredAt = LocalDateTime.now().plusMinutes(10),
                    otpId = it.otpId
                )
            }
    }
}
