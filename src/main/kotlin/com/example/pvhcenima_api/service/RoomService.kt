package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.request.RoomRequest
import com.example.pvhcenima_api.model.response.RoomResponse
import java.util.*

interface RoomService {
    fun addRoom(request: RoomRequest): RoomResponse
    fun getRoomById(roomId: UUID): RoomResponse
    fun updateRoom(roomId: UUID, request: RoomRequest): RoomResponse
    fun deleteRoom(roomId: UUID)

    // Get all rooms owned by current user (house owner)
    fun getMyRooms(): List<RoomResponse>

    // Get rooms by house
    fun getRoomsByHouse(houseId: UUID): List<RoomResponse>

    // Get rooms by floor
    fun getRoomsByFloor(floorId: UUID): List<RoomResponse>

    // Get available rooms in a house
    fun getAvailableRooms(houseId: UUID): List<RoomResponse>

    // Get rooms I'm renting (for renters)
    fun getMyRentedRooms(): List<RoomResponse>

    // Assign renter to room
    fun assignRenter(roomId: UUID, renterId: UUID): RoomResponse

    // Remove renter from room
    fun removeRenter(roomId: UUID): RoomResponse
}