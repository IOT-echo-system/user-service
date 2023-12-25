package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.*
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.service.AuthService
import com.shiviraj.iot.authService.service.TokenService
import com.shiviraj.iot.authService.testUtils.assertNextWith
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono

class AuthControllerTest {
    private val authService = mockk<AuthService>()
    private val tokenService = mockk<TokenService>()
    private val authController = AuthController(authService = authService, tokenService = tokenService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should sign up with user details`() {
        val userDetails = UserDetailsBuilder(
            userId = "userId",
            name = "username",
            email = "email",
            password = "password"
        ).build()
        every { authService.register(any()) } returns Mono.just(userDetails)

        val userSignUpRequest = UserSignUpRequest(name = "name", email = "email", password = "password")
        val response = authController.signUp(userSignUpRequest)


        assertNextWith(response) {
            it shouldBe UserSignUpResponse(email = "email", userId = "userId", name = "username")
            verify(exactly = 1) {
                authService.register(userSignUpRequest)
            }
        }
    }

    @Test
    fun `should login with credentials`() {
        val token = Token(tokenId = "tokenId", value = "value")
        every { tokenService.login(any()) } returns Mono.just(token)

        val userLoginRequest = UserLoginRequest(email = "email", password = "password")
        val response = authController.login(userLoginRequest)

        assertNextWith(response) {
            it shouldBe TokenResponse(token = "value")
            verify(exactly = 1) {
                tokenService.login(userLoginRequest)
            }
        }
    }

    @Test
    fun `should validate token`() {
        every { tokenService.validate(any()) } returns Mono.just(ValidateTokenResponse(userId = "userId"))

        val response = authController.validateToken("token")

        assertNextWith(response) {
            it shouldBe ValidateTokenResponse(userId = "userId")
            verify(exactly = 1) {
                tokenService.validate("token")
            }
        }
    }
}
