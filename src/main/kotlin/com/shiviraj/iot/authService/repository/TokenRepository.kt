package com.shiviraj.iot.authService.repository

import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.model.TokenId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.LocalDateTime

@Repository
interface TokenRepository : ReactiveCrudRepository<Token, TokenId> {
    fun findByValueAndExpiredAtAfter(value: String, expiredAt: LocalDateTime = LocalDateTime.now()): Mono<Token>
}
