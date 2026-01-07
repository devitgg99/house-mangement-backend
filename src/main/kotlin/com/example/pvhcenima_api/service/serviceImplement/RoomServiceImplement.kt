package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.Room
import com.example.pvhcenima_api.model.request.RoomRequest
import com.example.pvhcenima_api.model.response.RoomResponse
import com.example.pvhcenima_api.repository.FloorRepository
import com.example.pvhcenima_api.repository.RoomRepository
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.RoomService
import org.springframework.stereotype.Service
import java.util.*

@Service
class RoomServiceImplement(
    private val roomRepository: RoomRepository,
    private val floorRepository: FloorRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
) : RoomService {

    private fun Room.toResponse() = RoomResponse(
        roomId = this.roomId,
        roomName = this.roomName,
        price = this.price,
        floorId = this.floor.floorId,
        floorNumber = this.floor.floorNumber,
        floorName = this.floor.floorName,
        houseId = this.floor.house.houseId,
        houseName = this.floor.house.houseName,
        renterId = this.renter?.userId,
        renterName = this.renter?.fullName,
        available = this.renter == null,
        images = this.images
    )

    private fun checkFloorOwnership(floorId: UUID) {
        val floor = floorRepository.findById(floorId)
            .orElseThrow { IllegalArgumentException("Floor not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (floor.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't own this floor")
        }
    }

    private fun checkRoomOwnership(roomId: UUID): Room {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (room.floor.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to manage this room")
        }
        return room
    }

    // ==================== CRUD ====================

    override fun addRoom(request: RoomRequest): RoomResponse {
        checkFloorOwnership(request.floorId)

        val floor = floorRepository.findById(request.floorId)
            .orElseThrow { IllegalArgumentException("Floor not found") }

        val room = roomRepository.save(
            Room(
                roomName = request.roomName,
                price = request.price,
                floor = floor,
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

        val floor = floorRepository.findById(request.floorId)
            .orElseThrow { IllegalArgumentException("Floor not found") }

        // Verify the new floor also belongs to current user
        checkFloorOwnership(request.floorId)

        val updatedRoom = roomRepository.save(
            room.copy(
                roomName = request.roomName,
                price = request.price,
                floor = floor,
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
        return roomRepository.findAllByFloorHouseOwnerUserId(ownerId).map { it.toResponse() }
    }

    override fun getRoomsByHouse(houseId: UUID): List<RoomResponse> {
        return roomRepository.findAllByFloorHouseHouseId(houseId).map { it.toResponse() }
    }

    override fun getRoomsByFloor(floorId: UUID): List<RoomResponse> {
        return roomRepository.findAllByFloorFloorId(floorId).map { it.toResponse() }
    }

    override fun getAvailableRooms(houseId: UUID): List<RoomResponse> {
        return roomRepository.findAllByFloorHouseHouseIdAndRenterIsNull(houseId).map { it.toResponse() }
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