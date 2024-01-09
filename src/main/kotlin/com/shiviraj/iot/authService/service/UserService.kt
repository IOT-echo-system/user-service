package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.controller.view.ResetPasswordRequest
import com.shiviraj.iot.authService.controller.view.UserLoginRequest
import com.shiviraj.iot.authService.controller.view.UserSignUpRequest
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.model.UserId
import com.shiviraj.iot.authService.repository.UserRepository
import com.shiviraj.iot.loggingstarter.logOnError
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.userService.exceptions.BadDataException
import com.shiviraj.iot.userService.exceptions.DataNotFoundException
import com.shiviraj.iot.utils.service.IdGeneratorService
import com.shiviraj.iot.utils.utils.createMonoError
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserService(
    private val userRepository: UserRepository,
    private val idGeneratorService: IdGeneratorService,
    private val passwordEncoder: PasswordEncoder,
) {
    fun register(userDetails: UserSignUpRequest): Mono<UserDetails> {
        return userRepository.existsByEmail(userDetails.email)
            .flatMap {
                if (it) {
                    Mono.error(BadDataException(IOTError.IOT0101))
                } else {
                    idGeneratorService.generateId(IdType.USER_ID)
                        .flatMap { userId ->
                            val user =
                                UserDetails.from(userId, userDetails, passwordEncoder.encode(userDetails.password))
                            userRepository.save(user)
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

    fun verifyCredentials(userDetails: UserLoginRequest): Mono<UserDetails> {
        return userRepository.findByEmail(userDetails.email)
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

    fun resetPassword(userId: UserId, resetPasswordRequest: ResetPasswordRequest): Mono<UserDetails> {
        return userRepository.findByUserId(userId)
            .flatMap {
                this.verifyCredentials(
                    UserLoginRequest(email = it.email, password = resetPasswordRequest.currentPassword ?: "")
                )
            }
            .flatMap {
                userRepository.save(it.updatePassword(passwordEncoder.encode(resetPasswordRequest.password)))
            }
            .logOnSuccess(message = "Successfully updated password")
            .logOnError(errorMessage = "Failed to update password")
    }

    fun resetPasswordByEmail(email: String, password: String): Mono<UserDetails> {
        return userRepository.findByEmail(email)
            .flatMap {
                userRepository.save(it.updatePassword(passwordEncoder.encode(password)))
            }
            .logOnSuccess(message = "Successfully updated password using email")
            .logOnError(errorMessage = "Failed to update password using email")
    }

    fun getUserByEmail(email: String): Mono<UserDetails> {
        return userRepository.findByEmail(email)
            .switchIfEmpty {
                createMonoError(DataNotFoundException(IOTError.IOT0106))
            }
    }
}

