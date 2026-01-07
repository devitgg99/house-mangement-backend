package com.example.pvhcenima_api.model.request

import java.util.*

data class FloorRequest(
    val floorNumber: Int,
    val floorName: String? = null,
    val houseId: UUID
)

