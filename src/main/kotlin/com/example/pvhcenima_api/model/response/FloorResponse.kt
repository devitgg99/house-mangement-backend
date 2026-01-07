package com.example.pvhcenima_api.model.response

import java.util.*

data class FloorResponse(
    val floorId: UUID?,
    val floorNumber: Int,
    val floorName: String?,
    val houseId: UUID?,
    val houseName: String,
    val totalRooms: Long = 0
)

