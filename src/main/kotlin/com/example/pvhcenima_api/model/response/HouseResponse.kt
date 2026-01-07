package com.example.pvhcenima_api.model.response

import java.util.*

data class HouseResponse(
    val houseId: UUID?,
    val houseName: String,
    val houseAddress: String,
    val houseImage: String?,
    val totalFloors: Long = 0,
    val totalRooms: Long = 0,
    val floors: List<FloorSummary> = emptyList()
)