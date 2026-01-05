package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.Room
import com.example.pvhcenima_api.model.request.RoomRequest
import com.example.pvhcenima_api.model.response.RoomResponse
import com.example.pvhcenima_api.repository.HouseRepository
import com.example.pvhcenima_api.repository.RoomRepository
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.RoomService
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoomServiceImplement(
    private val roomRepository: RoomRepository,
    private val houseRepository: HouseRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
) : RoomService {

    private fun Room.toResponse() = RoomResponse(
        roomId = this.roomId,
        roomName = this.roomName,
        houseId = this.house.houseId,
        houseName = this.house.houseName,
        renterId = this.renter?.userId,
        renterName = this.renter?.fullName,
        available = this.renter == null,
        images = this.images
    )

    private fun checkHouseOwnership(houseId: UUID) {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't own this house")
        }
    }

    private fun checkRoomOwnership(roomId: UUID): Room {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (room.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to manage this room")
        }
        return room
    }

    // ==================== CRUD ====================

    override fun addRoom(request: RoomRequest): RoomResponse {
        checkHouseOwnership(request.houseId)

        val house = houseRepository.findById(request.houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        val room = roomRepository.save(
            Room(
                roomName = request.roomName,
                house = house,
                images = request.images
            )
        )
        return room.toResponse()
    }

    override fun getRoomById(roomId: UUID): RoomResponse {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found") }
        return room.toResponse()
    }

    override fun updateRoom(roomId: UUID, request: RoomRequest): RoomResponse {
        val room = checkRoomOwnership(roomId)

        val house = houseRepository.findById(request.houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        // Verify the new house also belongs to current user
        checkHouseOwnership(request.houseId)

        val updatedRoom = roomRepository.save(
            room.copy(
                roomName = request.roomName,
                house = house,
                images = request.images
            )
        )
        return updatedRoom.toResponse()
    }

    override fun deleteRoom(roomId: UUID) {
        val room = checkRoomOwnership(roomId)
        roomRepository.delete(room)
    }

    // ==================== Queries ====================

    override fun getMyRooms(): List<RoomResponse> {
        val ownerId = currentUserService.getCurrentUserId()
        return roomRepository.findAllByHouseOwnerUserId(ownerId).map { it.toResponse() }
    }

    override fun getRoomsByHouse(houseId: UUID): List<RoomResponse> {
        return roomRepository.findAllByHouseHouseId(houseId).map { it.toResponse() }
    }

    override fun getAvailableRooms(houseId: UUID): List<RoomResponse> {
        return roomRepository.findAllByHouseHouseIdAndRenterIsNull(houseId).map { it.toResponse() }
    }

    override fun getMyRentedRooms(): List<RoomResponse> {
        val renterId = currentUserService.getCurrentUserId()
        return roomRepository.findAllByRenterUserId(renterId).map { it.toResponse() }
    }

    // ==================== Renter Management ====================

    override fun assignRenter(roomId: UUID, renterId: UUID): RoomResponse {
        val room = checkRoomOwnership(roomId)

        if (room.renter != null) {
            throw IllegalArgumentException("Room already has a renter")
        }

        val renter = userRepository.findById(renterId)
            .orElseThrow { IllegalArgumentException("Renter not found") }

        val updatedRoom = roomRepository.save(room.copy(renter = renter))
        return updatedRoom.toResponse()
    }

    override fun removeRenter(roomId: UUID): RoomResponse {
        val room = checkRoomOwnership(roomId)

        if (room.renter == null) {
            throw IllegalArgumentException("Room has no renter")
        }

        val updatedRoom = roomRepository.save(room.copy(renter = null))
        return updatedRoom.toResponse()
    }
}