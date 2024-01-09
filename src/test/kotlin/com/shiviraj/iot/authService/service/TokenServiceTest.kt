package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.config.AppConfig
import com.shiviraj.iot.authService.controller.view.UserLoginRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.repository.TokenRepository
import com.shiviraj.iot.authService.testUtils.assertErrorWith
import com.shiviraj.iot.authService.testUtils.assertNextWith
import com.shiviraj.iot.userService.exceptions.UnAuthorizedException
import com.shiviraj.iot.utils.service.IdGeneratorService
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class TokenServiceTest {

    private val tokenRepository = mockk<TokenRepository>()
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val userService = mockk<UserService>()
    private val appConfig = AppConfig(secretKey = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey")

    private val tokenService = TokenService(
        tokenRepository = tokenRepository,
        idGeneratorService = idGeneratorService,
        userService = userService,
        appConfig = appConfig
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should create token if login credentials are correct`() {
        val credentials = UserLoginRequest(email = "email", password = "password")
        val user = UserDetailsBuilder(userId = "userId", email = "email", password = "encodedPassword").build()
        val token = Token(tokenId = "001", value = "token value")

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
            }
        }
    }

    @Test
    fun `should not validate token`() {
        val token = Token(tokenId = "001", value = "token value")
        val tokenValue = "token"

        every { tokenRepository.findByValue(any()) } returns Mono.just(token)

        val response = tokenService.validate(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify {
                tokenRepository.findByValue(tokenValue)
            }
        }
    }

    @Test
    fun `should not validate token if not exists in db`() {
        val tokenValue = "token"

        every { tokenRepository.findByValue(any()) } returns Mono.empty()

        val response = tokenService.validate(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify {
                tokenRepository.findByValue(tokenValue)
            }
        }
    }

    @Test
    fun `should generate temp token with otp`() {
        every { idGeneratorService.generateId(any()) } returns Mono.just("tokenId")
        val token = Token(tokenId = "adversarium", value = "cetero")
        every { tokenRepository.save(any()) } returns Mono.just(token)

        val otp = Otp(otpId = "otpId", value = "value", email = "example@email.com")
        val response = tokenService.generateTokenWithOtp(otp)

        assertNextWith(response) {
            it shouldBe token
            verify {
                idGeneratorService.generateId(IdType.TOKEN_ID)
                tokenRepository.save(any())
            }
        }
    }

    @Test
    fun `should not validate token for otp`() {
        val token = Token(tokenId = "001", value = "token value")
        val tokenValue = "token"

        every { tokenRepository.findByValue(any()) } returns Mono.just(token)

        val response = tokenService.validateTokenForOtp(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify {
                tokenRepository.findByValue(tokenValue)
            }
        }
    }

    @Test
    fun `should not validate token for otp if not exists in DB`() {
        val tokenValue = "token"

        every { tokenRepository.findByValue(any()) } returns Mono.empty()

        val response = tokenService.validateTokenForOtp(tokenValue)

        assertErrorWith(response) {
            it shouldBe UnAuthorizedException(IOTError.IOT0103)
            verify {
                tokenRepository.findByValue(tokenValue)
            }
        }
    }
}
