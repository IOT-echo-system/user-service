package com.robotutor.authService.service

import com.robotutor.authService.builder.TokenBuilder
import com.robotutor.authService.builder.UserDetailsBuilder
import com.robotutor.authService.controllers.view.ResetPasswordRequest
import com.robotutor.authService.controllers.view.UserLoginRequest
import com.robotutor.authService.exceptions.IOTError
import com.robotutor.authService.models.IdType
import com.robotutor.authService.repositories.TokenRepository
import com.robotutor.authService.services.TokenService
import com.robotutor.authService.services.UserService
import com.robotutor.authService.testUtils.assertErrorWith
import com.robotutor.authService.testUtils.assertNextWith
import com.robotutor.iot.exceptions.UnAuthorizedException
import com.robotutor.iot.models.AuditEvent
import com.robotutor.iot.models.AuditMessage
import com.robotutor.iot.models.AuditStatus
import com.robotutor.iot.models.MqttTopicName
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.services.MqttPublisher
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.time.ZoneId

class TokenServiceTest {

    private val tokenRepository = mockk<TokenRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val userService = mockk<UserService>()
    private val mqttPublisher = mockk<MqttPublisher>()

    private val tokenService = TokenService(
        tokenRepository = tokenRepository,
        idGeneratorService = idGeneratorService,
        userService = userService,
        accountServiceGateway = TODO()
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
    fun `should create token if login credentials are correct`() {
        val credentials = UserLoginRequest(email = "email", password = "password")
        val user = UserDetailsBuilder(userId = "userId", email = "email", password = "encodedPassword").build()
        val token = TokenBuilder(tokenId = "001", value = "token value").build()

        every { userService.verifyCredentials(any()) } returns Mono.just(user)
        every { idGeneratorService.generateId(any()) } returns Mono.just("001")
        every { tokenRepository.save(any()) } returns Mono.just(token)


        val response = tokenService.login(credentials)

        assertNextWith(response) {
            it shouldBe token
            verify {
                userService.verifyCredentials(credentials)
                idGeneratorService.generateId(IdType.TOKEN_ID)
                tokenRepository.save(any())
                mqttPublisher.publish(
                    MqttTopicName.AUDIT, AuditMessage(
                        status = AuditStatus.SUCCESS,
                        userId = "userId",
                        metadata = mapOf("tokenId" to "001"),
                        event = AuditEvent.GENERATE_TOKEN,
                        accountId = "missing-account-id",
                        deviceId = "missing-device-id",
                        timestamp = mockTime
                    )
                )
            }
        }
    }

    @Test
    fun `should not validate token`() {
        val tokenValue = "token"

        every { tokenRepository.findByValueAndExpiredAtAfter(any(), any()) } returns Mono.empty()

        val response = tokenService.validate(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify(exactly = 1) {
                tokenRepository.findByValueAndExpiredAtAfter(tokenValue, any())
            }
        }
    }

    @Test
    fun `should not validate token if not exists in db`() {
        val tokenValue = "token"

        every { tokenRepository.findByValueAndExpiredAtAfter(any(), any()) } returns Mono.empty()

        val response = tokenService.validate(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify {
                tokenRepository.findByValueAndExpiredAtAfter(tokenValue, any())
            }
        }
    }

    @Test
    fun `should reset user password`() {
        val userDetails = UserDetailsBuilder(userId = "userId").build()
        val token = TokenBuilder(userId = "userId", otpId = null).build()

        every { tokenRepository.findByValueAndExpiredAtAfter(any(), any()) } returns Mono.just(token)
        every { userService.resetPassword(any(), any(), any()) } returns Mono.just(userDetails)
        every { tokenRepository.save(any()) } returns Mono.just(token)

        val response = tokenService.resetPassword(ResetPasswordRequest("password", "new password"), "tokenValue")

        assertNextWith(response) {
            it shouldBe userDetails
            verify(exactly = 1) {
                tokenRepository.findByValueAndExpiredAtAfter("tokenValue", any())
                userService.resetPassword("userId", "password", "new password")
                tokenRepository.save(token)
            }
        }
    }

    @Test
    fun `should not reset user password if current password is not present`() {
        val token = TokenBuilder(userId = "userId", otpId = null).build()

        every { tokenRepository.findByValueAndExpiredAtAfter(any(), any()) } returns Mono.just(token)
        val unAuthorizedException = UnAuthorizedException(IOTError.IOT0103)
        every { userService.resetPassword(any(), any(), any()) } returns Mono.error(unAuthorizedException)

        val response = tokenService.resetPassword(
            ResetPasswordRequest(currentPassword = null, password = "password"),
            "tokenValue"
        )

        assertErrorWith(response) {
            it shouldBe unAuthorizedException
            verify(exactly = 1) {
                tokenRepository.findByValueAndExpiredAtAfter("tokenValue", any())
                userService.resetPassword("userId", "", "password")
            }
        }
    }

    @Test
    fun `should reset user password without current password`() {
        val userDetails = UserDetailsBuilder(userId = "userId").build()
        val token = TokenBuilder(userId = "userId", otpId = "otpId").build()

        every { tokenRepository.findByValueAndExpiredAtAfter(any(), any()) } returns Mono.just(token)
        every { userService.resetPassword(any(), any()) } returns Mono.just(userDetails)
        every { tokenRepository.save(any()) } returns Mono.just(token)

        val response = tokenService.resetPassword(
            ResetPasswordRequest(currentPassword = null, password = "password"),
            "tokenValue"
        )

        assertNextWith(response) {
            it shouldBe userDetails
            verify(exactly = 1) {
                tokenRepository.findByValueAndExpiredAtAfter("tokenValue", any())
                userService.resetPassword("userId", "password")
                tokenRepository.save(token)
            }
        }
    }
}
