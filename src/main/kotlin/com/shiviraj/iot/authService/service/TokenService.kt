package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.controller.view.UserLoginDetails
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.repository.TokenRepository
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.userService.exceptions.UnAuthorizedException
import com.shiviraj.iot.utils.service.IdGeneratorService
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty
import java.security.Key
import java.util.*

@Service
class TokenService(
    private val tokenRepository: TokenRepository,
    private val idGeneratorService: IdGeneratorService,
    private val authService: AuthService
) {
    private val SECRET = System.getenv("SECRET_KEY") ?: "sdfslkdjflsdfjsdofjpsfjlskdflksdfjisodfjsldfmlsdflksdgks"

    fun login(userLoginDetails: UserLoginDetails): Mono<Token> {
        return authService.verifyCredentials(userLoginDetails)
            .flatMap { userDetails ->
                val value = generateToken(userDetails)
                idGeneratorService.generateId(IdType.TOKEN_ID)
                    .flatMap { tokenId ->
                        tokenRepository.save(Token.from(tokenId = tokenId, value = value))
                    }
                    .logOnSuccess(
                        message = "Successfully logged in user",
                        searchableFields = mapOf("userId" to userDetails.userId)
                    )
                    .logOnSuccess(
                        message = "Failed to logg in user",
                        searchableFields = mapOf("userId" to userDetails.userId)
                    )
            }
    }

    fun validate(token: String): Mono<Boolean> {
        return tokenRepository.findByValue(token)
            .map {
                try {
                    Jwts.parserBuilder().setSigningKey(getSignKey()).build().parseClaimsJws(it.value)
                    true
                } catch (e: Exception) {
                    throw UnAuthorizedException(IOTError.IOT0103)
                }
            }
            .switchIfEmpty {
                Mono.error(UnAuthorizedException(IOTError.IOT0103))
            }
    }

    private fun generateToken(userDetails: UserDetails): String {
        return Jwts.builder()
            .setClaims(hashMapOf<String, Any>())
            .setSubject(userDetails.userId)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
            .signWith(getSignKey(), SignatureAlgorithm.HS256).compact();
    }

    private fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(SECRET)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
