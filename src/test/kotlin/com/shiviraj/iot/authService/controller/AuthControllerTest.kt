package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.*
import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.service.OtpService
import com.shiviraj.iot.authService.service.TokenService
import com.shiviraj.iot.authService.service.UserService
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
    private val userService = mockk<UserService>()
    private val tokenService = mockk<TokenService>()
    private val otpService = mockk<OtpService>()
    private val authController =
        AuthController(userService = userService, tokenService = tokenService, otpService = otpService)

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
        every { userService.register(any()) } returns Mono.just(userDetails)

        val userSignUpRequest = UserSignUpRequest(name = "name", email = "email", password = "password")
        val response = authController.signUp(userSignUpRequest)


        assertNextWith(response) {
            it shouldBe UserSignUpResponse(email = "email", userId = "userId", name = "username")
            verify(exactly = 1) {
                userService.register(userSignUpRequest)
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
            it shouldBe TokenResponse(token = "value", success = true)
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

    @Test
    fun `should generate otp`() {
        val otp = Otp(otpId = "otpID", value = "otp", email = "example@email.com")
        every { otpService.generateOtp(any()) } returns Mono.just(otp)

        val generateOtpRequest = GenerateOtpRequest(email = "example@email.com")
        val res = authController.generateOtp(generateOtpRequest)

        assertNextWith(res) {
            it shouldBe OtpResponse(otpId = "otpID", success = true, generateAt = otp.createdAt)

            verify(exactly = 1) {
                otpService.generateOtp(generateOtpRequest)
            }

        }
    }

    @Test
    fun `should verify otp`() {
        val token = Token(tokenId = "tokenId", value = "value")
        every { otpService.verifyOtp(any()) } returns Mono.just(token)


        val verifyOtpRequest = VerifyOtpRequest(otpId = "otpId", otp = "otp")
        val res = authController.verifyOtp(verifyOtpRequest)

        assertNextWith(res) {
            it shouldBe TokenResponse(token = "value", success = true)

            verify(exactly = 1) {
                otpService.verifyOtp(verifyOtpRequest)
            }
        }
    }

    @Test
    fun `should reset password`() {
        every { otpService.resetPassword(any(), any()) } returns Mono.just(UserDetailsBuilder().build())

        val resetPasswordRequest = ResetPasswordRequest(currentPassword = null, password = "Password")
        val res = authController.resetPassword(resetPasswordRequest = resetPasswordRequest)

        assertNextWith(res) {
            it shouldBe ResetPasswordResponse(success = true)

            verify(exactly = 1) {
                otpService.resetPassword(resetPasswordRequest = resetPasswordRequest, token = "")
            }
        }
    }
}
