package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.OtpBuilder
import com.shiviraj.iot.authService.builder.TokenBuilder
import com.shiviraj.iot.authService.controller.view.GenerateOtpRequest
import com.shiviraj.iot.authService.controller.view.VerifyOtpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.OtpState
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.repository.OtpRepository
import com.shiviraj.iot.authService.testUtils.assertErrorWith
import com.shiviraj.iot.authService.testUtils.assertNextWith
import com.shiviraj.iot.mqtt.model.*
import com.shiviraj.iot.mqtt.service.MqttPublisher
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.TooManyRequestsException
import com.shiviraj.iot.utils.service.IdGeneratorService
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

class OtpServiceTest {
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val otpRepository = mockk<OtpRepository>()
    private val tokenService = mockk<TokenService>()
    private val userService = mockk<UserService>()
    private val mqttPublisher = mockk<MqttPublisher>()
    private val otpService = OtpService(
        idGeneratorService = idGeneratorService,
        otpRepository = otpRepository,
        tokenService = tokenService,
        userService = userService,
        mqttPublisher = mqttPublisher
    )
    private val mockTime = LocalDateTime.of(2024, 1, 1, 1, 1)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkStatic(LocalDateTime::class)
        every { mqttPublisher.publish(any(), any()) } returns Unit
        every { LocalDateTime.now(ZoneId.of("UTC")) } returns mockTime

    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should generate otp for first time`() {
        val now = LocalDateTime.of(2022, 1, 1, 1, 10)
        val before10Min = LocalDateTime.of(2022, 1, 1, 1, 0)
        every { LocalDateTime.now() } returns now
        every { otpRepository.countByEmailAndCreatedAtAfter(any(), any()) } returns Mono.just(0)
        val otp = OtpBuilder(otpId = "otpId", value = "value", email = "example@email.com", createdAt = now).build()
        every { otpRepository.findByEmailAndState(any(), any()) } returns Mono.empty()
        every { userService.getUserByEmail(any()) } returns Mono.just(
            UserDetails(
                userId = "userId",
                name = "User",
                email = "example@email.com",
                password = "encodedPassword",
            )
        )
        every { idGeneratorService.generateId(any()) } returns Mono.just("otpId")
        every { otpRepository.save(any()) } returns Mono.just(otp)

        val generateOtp = otpService.generateOtp(GenerateOtpRequest("example@email.com"))

        assertNextWith(generateOtp) {
            it shouldBe otp
            verify(exactly = 1) {
                otpRepository.countByEmailAndCreatedAtAfter("example@email.com", before10Min)
                otpRepository.findByEmailAndState("example@email.com", OtpState.GENERATED)
                userService.getUserByEmail("example@email.com")
                idGeneratorService.generateId(IdType.OTP_ID)

                /*mqttPublisher.publish(
                    MqttTopicName.COMMUNICATION,
                    CommunicationMessage(
                        userId = "userId",
                        metadata = mapOf("name" to "User", "otp" to "value"),
                        type = CommunicationType.OTP,
                        to = "example@email.com"
                    )
                )*/
                mqttPublisher.publish(
                    MqttTopicName.AUDIT, AuditMessage(
                        status = AuditStatus.SUCCESS,
                        userId = "userId",
                        metadata = mapOf("otpId" to "otpId"),
                        event = AuditEvent.GENERATE_OTP,
                        accountId = "missing-account-id",
                        deviceId = "missing-device-id",
                        timestamp = mockTime
                    )
                )
            }
        }
    }

    @Test
    fun `should generate otp for second time`() {
        val now = LocalDateTime.of(2022, 1, 1, 1, 10)
        val before10Min = LocalDateTime.of(2022, 1, 1, 1, 0)
        every { LocalDateTime.now() } returns now

        every { otpRepository.countByEmailAndCreatedAtAfter(any(), any()) } returns Mono.just(1)
        val otp = OtpBuilder(otpId = "otpId", value = "value", email = "example@email.com", createdAt = now).build()
        every { otpRepository.findByEmailAndState(any(), any()) } returns Mono.just(otp)
        every { userService.getUserByEmail(any()) } returns Mono.just(
            UserDetails(
                userId = "userId",
                name = "User",
                email = "example@email.com",
                password = "encodedPassword"
            )
        )
        every { idGeneratorService.generateId(any()) } returns Mono.just("otpId")
        every { otpRepository.save(any()) } returns Mono.just(otp)

        val generateOtp = otpService.generateOtp(GenerateOtpRequest("example@email.com"))

        assertNextWith(generateOtp) {
            it shouldBe otp
            verify(exactly = 1) {
                otpRepository.countByEmailAndCreatedAtAfter("example@email.com", before10Min)
                otpRepository.findByEmailAndState("example@email.com", OtpState.GENERATED)
                userService.getUserByEmail("example@email.com")
                idGeneratorService.generateId(IdType.OTP_ID)
                mqttPublisher.publish(
                    MqttTopicName.AUDIT, AuditMessage(
                        status = AuditStatus.SUCCESS,
                        userId = "userId",
                        metadata = mapOf("otpId" to "otpId"),
                        event = AuditEvent.GENERATE_OTP,
                        accountId = "missing-account-id",
                        deviceId = "missing-device-id",
                        timestamp = mockTime
                    )
                )
            }
            verify {
                otpRepository.save(otp.copy(state = OtpState.EXPIRED))
            }
        }
    }

    @Test
    fun `should not generate otp for more than 3 times`() {
        val now = LocalDateTime.of(2022, 1, 1, 1, 10)
        val before10Min = LocalDateTime.of(2022, 1, 1, 1, 0)
        every { LocalDateTime.now() } returns now

        every { otpRepository.countByEmailAndCreatedAtAfter(any(), any()) } returns Mono.just(3)

        val generateOtp = otpService.generateOtp(GenerateOtpRequest("example@email.com"))

        assertErrorWith(generateOtp) {
            it shouldBe TooManyRequestsException(IOTError.IOT0104)
            verify(exactly = 1) {
                otpRepository.countByEmailAndCreatedAtAfter("example@email.com", before10Min)
            }
        }
    }

    @Test
    fun `should verify otp`() {
        val otp = OtpBuilder(userId = "userId", otpId = "otpId", value = "otp", email = "example@email.com").build()
        every { otpRepository.findByOtpIdAndState(any(), any()) } returns Mono.just(otp)
        every { otpRepository.save(any()) } returns Mono.just(otp)
        val token = TokenBuilder(tokenId = "tokenId", value = "token").build()
        every { tokenService.generateToken(any(), any(), any()) } returns Mono.just(token)

        val response = otpService.verifyOtp(VerifyOtpRequest(otpId = "otpId", otp = "otp"))

        assertNextWith(response) {
            it shouldBe token
            verify(exactly = 1) {
                otpRepository.findByOtpIdAndState("otpId", OtpState.GENERATED)
                otpRepository.save(otp.copy(state = OtpState.VERIFIED))
                mqttPublisher.publish(
                    MqttTopicName.AUDIT, AuditMessage(
                        status = AuditStatus.SUCCESS,
                        userId = "userId",
                        metadata = mapOf("otpId" to "otpId"),
                        event = AuditEvent.VERIFY_OTP,
                        accountId = "missing-account-id",
                        deviceId = "missing-device-id",
                        timestamp = mockTime
                    )
                )
            }
        }
    }

    @Test
    fun `should return error for invalid otp`() {
        val otp = OtpBuilder(userId = "userId", otpId = "otpId", value = "otp", email = "example@email.com").build()
        every { otpRepository.findByOtpIdAndState(any(), any()) } returns Mono.just(otp)

        val response = otpService.verifyOtp(VerifyOtpRequest(otpId = "otpId", otp = "invalidOtp"))

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0105)
            verify(exactly = 1) {
                otpRepository.findByOtpIdAndState("otpId", OtpState.GENERATED)
                mqttPublisher.publish(
                    MqttTopicName.AUDIT, AuditMessage(
                        status = AuditStatus.FAILURE,
                        userId = "userId",
                        metadata = mapOf("otpId" to "otpId"),
                        event = AuditEvent.VERIFY_OTP,
                        accountId = "missing-account-id",
                        deviceId = "missing-device-id",
                        timestamp = mockTime
                    )
                )
            }
        }
    }


}
