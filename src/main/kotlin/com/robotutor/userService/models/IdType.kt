package com.robotutor.userService.models

import com.robotutor.iot.service.IdSequenceType


enum class IdType(override val length: Int) : IdSequenceType {
    USER_ID(10),
}
