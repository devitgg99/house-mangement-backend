package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.request.FloorRequest
import com.example.pvhcenima_api.model.response.FloorResponse
import java.util.*

interface FloorService {
    fun addFloor(request: FloorRequest): FloorResponse
    fun getFloorById(floorId: UUID): FloorResponse
    fun updateFloor(floorId: UUID, request: FloorRequest): FloorResponse
    fun deleteFloor(floorId: UUID)
    
    // Get all floors in a house
    fun getFloorsByHouse(houseId: UUID): List<FloorResponse>
    
    // Get all floors owned by current user
    fun getMyFloors(): List<FloorResponse>
}

