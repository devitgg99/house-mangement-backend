package com.example.pvhcenima_api.model.request

import java.util.*

data class RoomRequest(
    val roomName: String,
    val houseId: UUID,
    val images: List<String> = emptyList()  // List of image URLs from S3
)

