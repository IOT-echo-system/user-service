package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.controller.view.*
import com.shiviraj.iot.authService.service.OtpService
import com.shiviraj.iot.authService.service.TokenService
import com.shiviraj.iot.authService.service.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(
    private val userService: UserService,
    private val tokenService: TokenService,
    private val otpService: OtpService
) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody @Validated userDetails: UserSignUpRequest): Mono<UserSignUpResponse> {
        return userService.register(userDetails).map { UserSignUpResponse.create(it) }
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated userDetails: UserLoginRequest): Mono<TokenResponse> {
        return tokenService.login(userDetails).map { TokenResponse(it.value, true) }
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") token: String = ""): Mono<ValidateTokenResponse> {
        return tokenService.validate(token)
    }

    @PostMapping("/generate-otp")
    fun generateOtp(@RequestBody @Validated generateOtpRequest: GenerateOtpRequest): Mono<OtpResponse> {
        return otpService.generateOtp(generateOtpRequest).map { OtpResponse(it.otpId, true, it.createdAt) }
    }

    @PostMapping("/verify-otp")
    fun verifyOtp(@RequestBody @Validated verifyOtpRequest: VerifyOtpRequest): Mono<TokenResponse> {
        return otpService.verifyOtp(verifyOtpRequest).map { TokenResponse(it.value, true) }
    }

    @PostMapping("/reset-password")
    fun resetPassword(
        @RequestBody @Validated resetPasswordRequest: ResetPasswordRequest,
        @RequestHeader("Authorization") token: String = ""
    ): Mono<ResetPasswordResponse> {
        return otpService.resetPassword(resetPasswordRequest, token).map { ResetPasswordResponse(true) }
    }
}
