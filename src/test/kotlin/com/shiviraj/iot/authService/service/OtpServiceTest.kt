package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.builder.UserDetailsBuilder
import com.shiviraj.iot.authService.controller.view.GenerateOtpRequest
import com.shiviraj.iot.authService.controller.view.ResetPasswordRequest
import com.shiviraj.iot.authService.controller.view.ValidateTokenResponse
import com.shiviraj.iot.authService.controller.view.VerifyOtpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.*
import com.shiviraj.iot.authService.repository.OtpRepository
import com.shiviraj.iot.authService.testUtils.assertErrorWith
import com.shiviraj.iot.authService.testUtils.assertNextWith
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.TooManyRequestsException
import com.shiviraj.iot.userService.exceptions.UnAuthorizedException
import com.shiviraj.iot.utils.service.IdGeneratorService
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import reactor.core.publisher.Mono
import java.time.LocalDateTime

class OtpServiceTest {
    private val idGeneratorService = mockk<IdGeneratorService>()
    private val otpRepository = mockk<OtpRepository>()
    private val tokenService = mockk<TokenService>()
    private val userService = mockk<UserService>()
    private val otpService = OtpService(
        idGeneratorService = idGeneratorService,
        otpRepository = otpRepository,
        tokenService = tokenService,
        userService = userService
    )

    @BeforeEach
    fun setUp() {
        clearAllMocks()
        mockkStatic(LocalDateTime::class)
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
        val otp = Otp(otpId = "otpId", value = "value", email = "example@email.com", createdAt = now)
        every { otpRepository.findByEmailAndState(any(), any()) } returns Mono.empty()
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
            }
        }
    }

    @Test
    fun `should generate otp for second time`() {
        val now = LocalDateTime.of(2022, 1, 1, 1, 10)
        val before10Min = LocalDateTime.of(2022, 1, 1, 1, 0)
        every { LocalDateTime.now() } returns now

        every { otpRepository.countByEmailAndCreatedAtAfter(any(), any()) } returns Mono.just(1)
        val otp = Otp(otpId = "otpId", value = "value", email = "example@email.com", createdAt = now)
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
        val otp = Otp(otpId = "otpId", value = "otp", email = "example@email.com")
        every { otpRepository.findByOtpIdAndState(any(), any()) } returns Mono.just(otp)
        every { otpRepository.save(any()) } returns Mono.just(otp)
        val token = Token(tokenId = "tokenId", value = "token")
        every { tokenService.generateTokenWithOtp(any()) } returns Mono.just(token)

        val response = otpService.verifyOtp(VerifyOtpRequest(otpId = "otpId", otp = "otp"))

        assertNextWith(response) {
            it shouldBe token
            verify(exactly = 1) {
                otpRepository.findByOtpIdAndState("otpId", OtpState.GENERATED)
                otpRepository.save(otp.copy(state = OtpState.VERIFIED))
            }
        }
    }

    @Test
    fun `should return error for invalid otp`() {
        val otp = Otp(otpId = "otpId", value = "otp", email = "example@email.com")
        every { otpRepository.findByOtpIdAndState(any(), any()) } returns Mono.just(otp)

        val response = otpService.verifyOtp(VerifyOtpRequest(otpId = "otpId", otp = "invalidOtp"))

        assertErrorWith(response) {
            it shouldBe BadDataException(IOTError.IOT0105)
            verify(exactly = 1) {
                otpRepository.findByOtpIdAndState("otpId", OtpState.GENERATED)
            }
        }
    }


    @Test
    fun `should reset user password`() {
        val userDetails = UserDetailsBuilder(userId = "userId").build()
        val resetPasswordRequest = ResetPasswordRequest(currentPassword = "Password", password = "New password")

        every { tokenService.validate(any()) } returns Mono.just(ValidateTokenResponse(userId = "userId"))
        every { userService.resetPassword(any(), any()) } returns Mono.just(userDetails)

        val response = otpService.resetPassword(resetPasswordRequest = resetPasswordRequest, token = "token")

        assertNextWith(response) {
            it shouldBe userDetails
            verify(exactly = 1) {
                tokenService.validate("token")
                userService.resetPassword("userId", resetPasswordRequest)
            }
        }
    }

    @Test
    fun `should reset user password with temp token`() {
        val userDetails = UserDetailsBuilder(userId = "userId").build()
        val otp = Otp(otpId = "otpId", value = "otp", email = "example@email.com", state = OtpState.VERIFIED)
        val resetPasswordRequest = ResetPasswordRequest(currentPassword = "Password", password = "New password")

        every { tokenService.validate(any()) } returns Mono.error(UnAuthorizedException(IOTError.IOT0103))
        every { tokenService.validateTokenForOtp(any()) } returns Mono.just("otpId")
        every { otpRepository.findByOtpIdAndState(any(), any()) } returns Mono.just(otp)
        every { userService.resetPasswordByEmail(any(), any()) } returns Mono.just(userDetails)

        val response = otpService.resetPassword(resetPasswordRequest = resetPasswordRequest, token = "token")

        assertNextWith(response) {
            it shouldBe userDetails
            verify(exactly = 1) {
                tokenService.validate("token")
                tokenService.validateTokenForOtp("token")
                otpRepository.findByOtpIdAndState("otpId", OtpState.VERIFIED)
                userService.resetPasswordByEmail("example@email.com", resetPasswordRequest.password)
            }
        }
    }
}
