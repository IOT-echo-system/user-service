package com.shiviraj.iot.authService.model

import com.shiviraj.iot.utils.service.IdSequenceType

enum class IdType(override val length: Int) : IdSequenceType {
    USER_ID(10),
    TOKEN_ID(16)
}
