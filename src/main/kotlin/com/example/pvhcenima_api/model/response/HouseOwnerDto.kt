package com.example.pvhcenima_api.model.response

import java.util.*

data class HouseOwnerDto(
    val userId: UUID,
    val fullName: String,
    val profileImage: String?,
    val email: String?
)
