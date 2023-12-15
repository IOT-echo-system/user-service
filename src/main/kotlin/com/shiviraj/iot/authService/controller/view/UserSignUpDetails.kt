package com.shiviraj.iot.authService.controller.view

import com.shiviraj.iot.authService.model.UserId

data class UserSignUpDetails(val name: String, val email: String, val password: String)
data class UserLoginDetails(val email: String, val password: String)
data class TokenResponse(val token: String)
data class ValidateTokenResponse(val userId: UserId)

