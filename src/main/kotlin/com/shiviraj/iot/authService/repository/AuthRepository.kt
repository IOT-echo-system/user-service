package com.shiviraj.iot.authService.repository

import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.model.UserId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface AuthRepository : ReactiveCrudRepository<UserDetails, UserId> {
    fun findByEmail(email: String): Mono<UserDetails>
    fun existsByEmail(email: String): Mono<Boolean>
}
