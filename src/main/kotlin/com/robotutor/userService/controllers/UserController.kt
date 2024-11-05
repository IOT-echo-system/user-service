package com.robotutor.userService.controllers

import com.robotutor.userService.controllers.view.UserDetailsResponse
import com.robotutor.userService.controllers.view.UserRegistrationRequest
import com.robotutor.userService.controllers.view.UserSignUpResponse
import com.robotutor.userService.models.UserDetails
import com.robotutor.userService.services.UserService
import com.robotutor.iot.utils.models.UserAuthenticationData
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
) {
    @PostMapping("/registration")
    fun registration(@RequestBody @Validated userDetails: UserRegistrationRequest): Mono<UserSignUpResponse> {
        return userService.register(userDetails).map { UserSignUpResponse.create(it) }
    }

    @GetMapping("/user-details")
    fun userDetails(authenticationData: UserAuthenticationData): Mono<UserDetailsResponse> {
        return userService.getUserByUserId(authenticationData.userId)
            .map { UserDetailsResponse.from(it, authenticationData) }
    }

    @GetMapping
    fun getUserId(@RequestParam email: String): Mono<UserDetails> {
        return userService.getUserByEmail(email)
    }
}
