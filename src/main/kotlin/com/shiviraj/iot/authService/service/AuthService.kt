package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.controller.view.UserSignUpDetails
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.repository.AuthRepository
import com.shiviraj.iot.loggingstarter.logOnError
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.utils.service.IdGeneratorService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class AuthService(
    private val authRepository: AuthRepository,
    private val idGeneratorService: IdGeneratorService,
    private val passwordEncoder: PasswordEncoder,
) {
    fun register(userDetails: UserSignUpDetails): Mono<UserDetails> {
        return authRepository.existsByEmail(userDetails.email)
            .flatMap {
                if (it) {
                    Mono.error(BadDataException(IOTError.IOT0101))
                } else {
                    idGeneratorService.generateId(IdType.USER_ID)
                        .flatMap { userId ->
                            val user =
                                UserDetails.from(userId, userDetails, passwordEncoder.encode(userDetails.password))
                            authRepository.save(user)
                        }
                        .logOnSuccess(
                            message = "Successfully registered new User",
                            searchableFields = mapOf("email" to userDetails.email)
                        )
                        .logOnError(
                            errorMessage = "Failed to register new User",
                            searchableFields = mapOf("email" to userDetails.email)
                        )

                }
            }
    }

    fun verifyCredentials(userDetails: UserLoginDetails): Mono<UserDetails> {
        return authRepository.findByEmail(userDetails.email)
            .flatMap { details ->
                val matches = passwordEncoder.matches(userDetails.password, details.password)
                Mono.deferContextual {
                    if (matches) {
                        Mono.just(details)
                    } else {
                        Mono.error(BadDataException(IOTError.IOT0102))
                    }
                }
            }
            .switchIfEmpty {
                Mono.error(BadDataException(IOTError.IOT0102))
            }
    }
}

