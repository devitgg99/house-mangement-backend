package com.example.pvhcenima_api.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.*

data class RoomResponse(
    val roomId: UUID?,
    val roomName: String,
    val houseId: UUID?,
    val houseName: String,
    val renterId: UUID?,
    val renterName: String?,
    @get:JsonProperty("isAvailable")
    val available: Boolean,
    val images: List<String> = emptyList()
)

