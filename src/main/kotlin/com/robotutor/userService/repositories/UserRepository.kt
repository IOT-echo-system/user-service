package com.robotutor.userService.repositories

import com.robotutor.userService.models.UserDetails
import com.robotutor.userService.models.UserId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface UserRepository : ReactiveCrudRepository<UserDetails, UserId> {
    fun existsByEmail(email: String): Mono<Boolean>
    fun findByEmail(email: String): Mono<UserDetails>
    fun findByUserId(userId: UserId): Mono<UserDetails>
}
