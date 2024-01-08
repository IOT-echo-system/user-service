package com.shiviraj.iot.authService.service

import com.shiviraj.iot.authService.config.AppConfig
import com.shiviraj.iot.authService.controller.view.UserLoginRequest
import com.shiviraj.iot.authService.controller.view.ValidateTokenResponse
import com.shiviraj.iot.authService.exception.IOTError
import com.shiviraj.iot.authService.model.IdType
import com.shiviraj.iot.authService.model.Otp
import com.shiviraj.iot.authService.model.Token
import com.shiviraj.iot.authService.model.UserDetails
import com.shiviraj.iot.authService.repository.TokenRepository
import com.shiviraj.iot.loggingstarter.logOnError
import com.shiviraj.iot.loggingstarter.logOnSuccess
import com.shiviraj.iot.userService.exceptions.UnAuthorizedException
import com.shiviraj.iot.utils.service.IdGeneratorService
import com.shiviraj.iot.utils.utils.createMono
import com.shiviraj.iot.utils.utils.createMonoError
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
    private val userService: UserService,
    private val appConfig: AppConfig
) {

    fun login(userLoginRequest: UserLoginRequest): Mono<Token> {
        return userService.verifyCredentials(userLoginRequest)
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
                    .logOnError(
                        errorMessage = "Failed to log in user",
                        searchableFields = mapOf("userId" to userDetails.userId)
                    )
            }
    }

    fun validate(token: String): Mono<ValidateTokenResponse> {
        return tokenRepository.findByValue(token)
            .flatMap {
                try {
                    val parseClaimsJws = Jwts.parserBuilder()
                        .setSigningKey(getSignKey())
                        .build()
                        .parseClaimsJws(it.value)
                    createMono(ValidateTokenResponse(parseClaimsJws.body.get("userId", String::class.java)))
                } catch (e: Exception) {
                    createMonoError(UnAuthorizedException(IOTError.IOT0103))
                }
            }
            .switchIfEmpty {
                Mono.error(UnAuthorizedException(IOTError.IOT0103))
            }
            .logOnSuccess(message = "Successfully validated authorization")
            .logOnError(errorCode = IOTError.IOT0103.errorCode, errorMessage = "Failed to validate authorization")
    }


    fun generateTokenWithOtp(otp: Otp): Mono<Token> {
        return idGeneratorService.generateId(IdType.TOKEN_ID)
            .flatMap { tokenId ->
                val value = generateTempToken(otp)
                tokenRepository.save(Token.from(tokenId = tokenId, value = value))
            }
            .logOnSuccess(message = "Successfully generated temp token", searchableFields = mapOf("otpId" to otp.otpId))
            .logOnError(errorMessage = "Failed to generate temp token", searchableFields = mapOf("otpId" to otp.otpId))

    }

//    fun validateTokenForOtp(token: String): Mono<String> {
//        return tokenRepository.findByValue(token)
//            .flatMap {
//                try {
//                    val parseClaimsJws = Jwts.parserBuilder()
//                        .setSigningKey(getSignKey())
//                        .build()
//                        .parseClaimsJws(it.value)
//
//                    createMono(parseClaimsJws.body.get("otpId", String::class.java))
//                } catch (e: Exception) {
//                    createMonoError(UnAuthorizedException(IOTError.IOT0103))
//                }
//            }
//            .switchIfEmpty {
//                Mono.error(UnAuthorizedException(IOTError.IOT0103))
//            }
//            .logOnSuccess(message = "Successfully validated authorization")
//            .logOnError(errorCode = IOTError.IOT0103.errorCode, errorMessage = "Failed to validate authorization")
//    }
//

    private fun generateTempToken(otp: Otp): String {
        return Jwts.builder()
            .setClaims(hashMapOf<String, Any>("optId" to otp.otpId))
            .setSubject(otp.otpId)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 10))
            .signWith(getSignKey(), SignatureAlgorithm.HS256).compact()
    }

    private fun generateToken(userDetails: UserDetails): String {
        return Jwts.builder()
            .setClaims(hashMapOf<String, Any>("userId" to userDetails.userId))
            .setSubject(userDetails.userId)
            .setIssuedAt(Date(System.currentTimeMillis()))
            .setExpiration(Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24 * 7))
            .signWith(getSignKey(), SignatureAlgorithm.HS256).compact()
    }

    private fun getSignKey(): Key {
        val keyBytes = Decoders.BASE64.decode(appConfig.secretKey)
        return Keys.hmacShaKeyFor(keyBytes)
    }
}
