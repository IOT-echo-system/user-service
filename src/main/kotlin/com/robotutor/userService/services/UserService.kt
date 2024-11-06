package com.robotutor.userService.services

import com.robotutor.iot.auditOnSuccess
import com.robotutor.iot.exceptions.BadDataException
import com.robotutor.iot.exceptions.DataNotFoundException
import com.robotutor.iot.models.AuditEvent
import com.robotutor.iot.service.IdGeneratorService
import com.robotutor.iot.utils.createMonoError
import com.robotutor.loggingstarter.logOnError
import com.robotutor.loggingstarter.logOnSuccess
import com.robotutor.userService.controllers.view.UserRegistrationRequest
import com.robotutor.userService.exceptions.IOTError
import com.robotutor.userService.gateway.AuthServiceGateway
import com.robotutor.userService.models.IdType
import com.robotutor.userService.models.UserDetails
import com.robotutor.userService.models.UserId
import com.robotutor.userService.repositories.UserRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class UserService(
    private val userRepository: UserRepository,
    private val idGeneratorService: IdGeneratorService,
    private val authServiceGateway: AuthServiceGateway
) {
    fun register(userDetails: UserRegistrationRequest): Mono<UserDetails> {
        return userRepository.findByEmail(userDetails.email)
            .flatMap {
                createMonoError<UserDetails>(BadDataException(IOTError.IOT0201))
            }
            .switchIfEmpty {
                idGeneratorService.generateId(IdType.USER_ID)
                    .flatMap { userId ->
                        val user = UserDetails.from(userId = userId, userDetails = userDetails)
                        userRepository.save(user)
                            .auditOnSuccess(event = AuditEvent.SIGN_UP, userId = userId)
                    }
            }
            .flatMap { user ->
                authServiceGateway.saveUserPassword(user.userId, userDetails.password).map { user }
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

    fun getUserByEmail(email: String): Mono<UserDetails> {
        return userRepository.findByEmail(email)
            .switchIfEmpty {
                createMonoError(DataNotFoundException(IOTError.IOT0202))
            }
    }

    fun getUserByUserId(userId: UserId): Mono<UserDetails> {
        return userRepository.findByUserId(userId)
            .switchIfEmpty {
                createMonoError(DataNotFoundException(IOTError.IOT0203))
            }
    }
}

