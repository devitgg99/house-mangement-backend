package com.example.pvhcenima_api.model.request

import java.math.BigDecimal
import java.util.*

data class RoomRequest(
    val roomName: String,
    val price: BigDecimal,
    val floorId: UUID,
    val images: List<String> = emptyList()  // List of image URLs from S3
)

