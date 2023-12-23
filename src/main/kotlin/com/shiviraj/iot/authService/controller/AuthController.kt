package com.shiviraj.iot.authService.controller

import com.shiviraj.iot.authService.controller.view.TokenResponse
import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.controller.view.UserSignUpDetails
import com.shiviraj.iot.authService.controller.view.ValidateTokenResponse
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.service.AuthService
import com.shiviraj.iot.authService.service.TokenService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/auth")
class AuthController(private val authService: AuthService, private val tokenService: TokenService) {

    @PostMapping("/sign-up")
    fun signUp(@RequestBody @Validated userDetails: UserSignUpDetails): Mono<UserDetails> {
        return authService.register(userDetails)
    }

    @PostMapping("/login")
    fun login(@RequestBody @Validated userDetails: UserLoginDetails): Mono<TokenResponse> {
        return tokenService.login(userDetails).map { TokenResponse(it.value) }
    }

    @GetMapping("/validate")
    fun validateToken(@RequestHeader("Authorization") token: String = ""): Mono<ValidateTokenResponse> {
        return tokenService.validate(token)
    }
}
