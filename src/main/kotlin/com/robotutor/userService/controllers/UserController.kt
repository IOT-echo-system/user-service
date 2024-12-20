package com.robotutor.userService.controllers

import com.robotutor.iot.utils.models.UserData
import com.robotutor.userService.controllers.view.UserDetailsView
import com.robotutor.userService.controllers.view.UserRegistrationRequest
import com.robotutor.userService.models.UserDetails
import com.robotutor.userService.services.UserService
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/users")
class UserController(private val userService: UserService) {
    @PostMapping("/registration")
    fun registration(@RequestBody @Validated userDetails: UserRegistrationRequest): Mono<UserDetailsView> {
        return userService.register(userDetails).map { UserDetailsView.from(it) }
    }

    @GetMapping("/me")
    fun getMyDetails(userData: UserData): Mono<UserDetailsView> {
        return userService.getMyDetails(userData).map { UserDetailsView.from(it) }
    }

//    @GetMapping("/user-details")
//    fun userDetails(authenticationData: UserAuthenticationData): Mono<UserDetailsResponse> {
//        return userService.getUserByUserId(authenticationData.userId)
//            .map { UserDetailsResponse.from(it, authenticationData) }
//    }

    @GetMapping
    fun getUserId(@RequestParam email: String): Mono<UserDetails> {
        return userService.getUserByEmail(email)
    }
}
