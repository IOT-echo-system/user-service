package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.controller.view.GenerateOtpRequest
import com.shiviraj.iot.authService.controller.view.VerifyOtpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.*
import com.shiviraj.iot.authService.repository.OtpRepository
import com.shiviraj.iot.loggingstarter.logOnError
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.mqtt.model.AuditEvent
import com.shiviraj.iot.mqtt.model.CommunicationMessage
import com.shiviraj.iot.mqtt.model.CommunicationType
import com.shiviraj.iot.mqtt.model.MqttTopicName
import com.shiviraj.iot.mqtt.service.MqttPublisher
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.TooManyRequestsException
import com.shiviraj.iot.utils.audit.auditOnError
import com.shiviraj.iot.utils.audit.auditOnSuccess
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
    private val userService: UserService,
    private val mqttPublisher: MqttPublisher
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
                            .map { sendEmail(it, userDetails) }
                            .auditOnSuccess(
                                mqttPublisher = mqttPublisher,
                                event = AuditEvent.GENERATE_OTP,
                                metadata = mapOf("otpId" to otpId),
                                userId = userDetails.userId
                            )
                    }
                    .auditOnError(
                        mqttPublisher = mqttPublisher,
                        event = AuditEvent.GENERATE_OTP,
                        userId = userDetails.userId
                    )
            }
            .logOnSuccess(message = "Successfully generated otp")
            .logOnError(errorMessage = "Failed to generate otp")
    }

    fun verifyOtp(verifyOtpRequest: VerifyOtpRequest): Mono<Token> {
        return otpRepository.findByOtpIdAndState(verifyOtpRequest.otpId, OtpState.GENERATED)
            .flatMap {
                if (it.isValidOtp(verifyOtpRequest.otp)) {
                    otpRepository.save(it.setVerified())
                        .auditOnSuccess(
                            mqttPublisher = mqttPublisher,
                            event = AuditEvent.VERIFY_OTP,
                            metadata = mapOf("otpId" to it.otpId),
                            userId = it.userId
                        )
                } else {
                    createMonoError<Otp>(BadDataException(IOTError.IOT0105))
                        .auditOnError(
                            mqttPublisher = mqttPublisher,
                            event = AuditEvent.VERIFY_OTP,
                            metadata = mapOf("otpId" to it.otpId),
                            userId = it.userId
                        )
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

    private fun sendEmail(otp: Otp, userDetails: UserDetails): Otp {
        mqttPublisher.publish(
            MqttTopicName.COMMUNICATION, CommunicationMessage(
                type = CommunicationType.OTP,
                to = otp.email,
                userId = otp.userId,
                metadata = mapOf("name" to userDetails.name, "otp" to otp.value)
            )
        )
        return otp
    }
}
