package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.controller.view.*
import com.shiviraj.iot.authService.service.AuthService
import com.shiviraj.iot.authService.service.TokenService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService, private val tokenService: TokenService) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody @Validated userDetails: UserSignUpRequest): Mono<UserSignUpResponse> {
        return authService.register(userDetails).map { UserSignUpResponse.create(it) }
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated userDetails: UserLoginRequest): Mono<TokenResponse> {
        return tokenService.login(userDetails).map { TokenResponse(it.value) }
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") token: String = ""): Mono<ValidateTokenResponse> {
        return tokenService.validate(token)
    }
}
