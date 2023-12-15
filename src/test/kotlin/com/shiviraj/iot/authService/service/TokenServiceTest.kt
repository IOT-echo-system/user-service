package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.config.AppConfig
import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
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
    private val authService = mockk<AuthService>()
    private val appConfig = AppConfig(secretKey = "secretkeysecretkeysecretkeysecretkeysecretkeysecretkeysecretkey")

    private val tokenService = TokenService(
        tokenRepository = tokenRepository,
        idGeneratorService = idGeneratorService,
        authService = authService,
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
        val credentials = UserLoginDetails(email = "email", password = "password")
        val user = UserDetailsBuilder(userId = "userId", email = "email", password = "encodedPassword").build()
        val token = Token(tokenId = "001", value = "token value")

        every { authService.verifyCredentials(any()) } returns Mono.just(user)
        every { idGeneratorService.generateId(any()) } returns Mono.just("001")
        every { tokenRepository.save(any()) } returns Mono.just(token)


        val response = tokenService.login(credentials)

        assertNextWith(response) {
            it shouldBe token
            verify {
                authService.verifyCredentials(credentials)
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
}
