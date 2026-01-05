package com.example.pvhcenima_api.model.request


data class HouseRequest(
    val houseName: String,
    val houseAddress: String,
    val houseImage: String?
)