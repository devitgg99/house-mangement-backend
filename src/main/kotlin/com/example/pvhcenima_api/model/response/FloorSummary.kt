package com.example.pvhcenima_api.model.response

import java.util.*

/**
 * Simplified floor info for embedding in HouseResponse
 * (doesn't include house info since it's already in the parent)
 */
data class FloorSummary(
    val floorId: UUID?,
    val floorNumber: Int,
    val floorName: String?,
    val totalRooms: Long = 0
)

