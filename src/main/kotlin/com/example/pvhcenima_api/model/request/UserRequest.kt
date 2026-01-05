package com.example.pvhcenima_api.model.request

import com.example.pvhcenima_api.model.entity.Role

data class UserRequest(
    val fullName: String,
    val phoneNumber: String,
    val password: String,
    val email: String,
    val role: Role
)