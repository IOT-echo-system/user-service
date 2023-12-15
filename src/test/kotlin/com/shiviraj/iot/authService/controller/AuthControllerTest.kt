package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.TokenResponse
import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.controller.view.UserSignUpDetails
import com.shiviraj.iot.authService.controller.view.ValidateTokenResponse
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
        val userDetails = UserDetailsBuilder().build()
        every { authService.register(any()) } returns Mono.just(userDetails)

        val userSignUpDetails = UserSignUpDetails(name = "name", email = "email", password = "password")
        val response = authController.signUp(userSignUpDetails)


        assertNextWith(response) {
            it shouldBe userDetails
            verify(exactly = 1) {
                authService.register(userSignUpDetails)
            }
        }
    }

    @Test
    fun `should login with credentials`() {
        val token = Token(tokenId = "tokenId", value = "value")
        every { tokenService.login(any()) } returns Mono.just(token)

        val userLoginDetails = UserLoginDetails(email = "email", password = "password")
        val response = authController.login(userLoginDetails)

        assertNextWith(response) {
            it shouldBe TokenResponse(token = "value")
            verify(exactly = 1) {
                tokenService.login(userLoginDetails)
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
