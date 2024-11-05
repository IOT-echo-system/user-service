package com.robotutor.userService.controller

import com.robotutor.userService.controllers.UserController
import com.robotutor.userService.services.UserService
import io.mockk.clearAllMocks
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

class UserControllerTest {
    private val userService = mockk<UserService>()
    private val userController = UserController(userService = userService)

    @BeforeEach
    fun setUp() {
        clearAllMocks()
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    /*@Test
    fun `should sign up with user details`() {
        val userDetails = UserDetailsBuilder(
            userId = "userId", name = "username", email = "email", password = "password"
        ).build()
        every { userService.register(any()) } returns Mono.just(userDetails)

        val userRegistrationRequest = UserRegistrationRequest(name = "name", email = "email", password = "password")
        val response = userController.signUp(userRegistrationRequest)


        assertNextWith(response) {
            it shouldBe UserSignUpResponse(email = "email", userId = "userId", name = "username")
            verify(exactly = 1) {
                userService.register(userRegistrationRequest)
            }
        }
    }

    @Test
    fun `should login with credentials`() {
        val token = TokenBuilder(tokenId = "tokenId", value = "value", userId = "userId").build()
        every { tokenService.login(any()) } returns Mono.just(token)

        val userLoginRequest = UserLoginRequest(email = "email", password = "password")
        val response = userController.login(userLoginRequest)

        assertNextWith(response) {
            it shouldBe TokenResponse(token = "value", success = true)
            verify(exactly = 1) {
                tokenService.login(userLoginRequest)
            }
        }
    }

    @Test
    fun `should validate token`() {
        every { tokenService.validate(any()) } returns Mono.just(
            ValidateTokenResponse(
                userId = "userId",
                projectId = "projectId",
                roleId = "roleId",
                boardId = null,
            )
        )

        val response = userController.validateToken("token")

        assertNextWith(response) {
            it shouldBe ValidateTokenResponse(
                userId = "userId", projectId = "projectId",
                roleId = "roleId",
                boardId = null,
            )
            verify(exactly = 1) {
                tokenService.validate("token")
            }
        }
    }

    @Test
    fun `should validate token with default`() {
        every { tokenService.validate(any()) } returns Mono.just(
            ValidateTokenResponse(
                userId = "userId", projectId = "projectId",
                roleId = "roleId",
                boardId = null,
            )
        )

        val response = userController.validateToken()

        assertNextWith(response) {
            it shouldBe ValidateTokenResponse(
                userId = "userId", projectId = "projectId",
                roleId = "roleId",
                boardId = null,
            )
            verify(exactly = 1) {
                tokenService.validate("")
            }
        }
    }

    @Test
    fun `should generate otp`() {
        val otp = OtpBuilder(otpId = "otpID", value = "otp", email = "example@email.com").build()
        every { otpService.generateOtp(any()) } returns Mono.just(otp)

        val generateOtpRequest = GenerateOtpRequest(email = "example@email.com")
        val res = userController.generateOtp(generateOtpRequest)

        assertNextWith(res) {
            it shouldBe OtpResponse(otpId = "otpID", success = true, generateAt = otp.createdAt)

            verify(exactly = 1) {
                otpService.generateOtp(generateOtpRequest)
            }

        }
    }

    @Test
    fun `should verify otp`() {
        val token = TokenBuilder(tokenId = "tokenId", value = "value").build()
        every { otpService.verifyOtp(any()) } returns Mono.just(token)


        val verifyOtpRequest = VerifyOtpRequest(otpId = "otpId", otp = "otp")
        val res = userController.verifyOtp(verifyOtpRequest)

        assertNextWith(res) {
            it shouldBe TokenResponse(token = "value", success = true)

            verify(exactly = 1) {
                otpService.verifyOtp(verifyOtpRequest)
            }
        }
    }

    @Test
    fun `should reset password`() {
        every { tokenService.resetPassword(any(), any()) } returns Mono.just(UserDetailsBuilder().build())

        val resetPasswordRequest = ResetPasswordRequest(currentPassword = null, password = "Password")
        val res = userController.resetPassword(resetPasswordRequest = resetPasswordRequest)

        assertNextWith(res) {
            it shouldBe ResetPasswordResponse(success = true)

            verify(exactly = 1) {
                tokenService.resetPassword(resetPasswordRequest = resetPasswordRequest, tokenValue = "")
            }
        }
    }*/
}
