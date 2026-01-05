package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.request.UserLogin
import com.example.pvhcenima_api.model.request.UserRequest


interface AuthService {
    fun register(userRequest: UserRequest)
    fun login(userRequest: UserLogin): String
}