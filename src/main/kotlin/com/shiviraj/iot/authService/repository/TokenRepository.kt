package com.shiviraj.iot.authService.repository

import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.model.TokenId
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
interface TokenRepository : ReactiveCrudRepository<Token, TokenId> {
    fun findByValue(value: String): Mono<Token>
}
