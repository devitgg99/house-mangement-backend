package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.Floor
import com.example.pvhcenima_api.model.request.FloorRequest
import com.example.pvhcenima_api.model.response.FloorResponse
import com.example.pvhcenima_api.repository.FloorRepository
import com.example.pvhcenima_api.repository.HouseRepository
import com.example.pvhcenima_api.repository.RoomRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.FloorService
import org.springframework.stereotype.Service
import java.util.*

@Service
class FloorServiceImplement(
    private val floorRepository: FloorRepository,
    private val houseRepository: HouseRepository,
    private val roomRepository: RoomRepository,
    private val currentUserService: CurrentUserService
) : FloorService {

    private fun Floor.toResponse() = FloorResponse(
        floorId = this.floorId,
        floorNumber = this.floorNumber,
        floorName = this.floorName,
        houseId = this.house.houseId,
        houseName = this.house.houseName,
        totalRooms = roomRepository.countByFloorFloorId(this.floorId!!)
    )

    private fun checkHouseOwnership(houseId: UUID) {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't own this house")
        }
    }

    private fun checkFloorOwnership(floorId: UUID): Floor {
        val floor = floorRepository.findById(floorId)
            .orElseThrow { IllegalArgumentException("Floor not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (floor.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to manage this floor")
        }
        return floor
    }

    override fun addFloor(request: FloorRequest): FloorResponse {
        checkHouseOwnership(request.houseId)

        // Check if floor number already exists
        if (floorRepository.existsByHouseHouseIdAndFloorNumber(request.houseId, request.floorNumber)) {
            throw IllegalArgumentException("Floor ${request.floorNumber} already exists in this house")
        }

        val house = houseRepository.findById(request.houseId)
            .orElseThrow { IllegalArgumentException("House not found") }

        val floor = floorRepository.save(
            Floor(
                floorNumber = request.floorNumber,
                floorName = request.floorName,
                house = house
            )
        )
        return floor.toResponse()
    }

    override fun getFloorById(floorId: UUID): FloorResponse {
        val floor = floorRepository.findById(floorId)
            .orElseThrow { IllegalArgumentException("Floor not found") }
        return floor.toResponse()
    }

    override fun updateFloor(floorId: UUID, request: FloorRequest): FloorResponse {
        val floor = checkFloorOwnership(floorId)

        // Check if new floor number conflicts (if changed)
        if (floor.floorNumber != request.floorNumber) {
            if (floorRepository.existsByHouseHouseIdAndFloorNumber(floor.house.houseId!!, request.floorNumber)) {
                throw IllegalArgumentException("Floor ${request.floorNumber} already exists in this house")
            }
        }

        val updatedFloor = floorRepository.save(
            floor.copy(
                floorNumber = request.floorNumber,
                floorName = request.floorName
            )
        )
        return updatedFloor.toResponse()
    }

    override fun deleteFloor(floorId: UUID) {
        val floor = checkFloorOwnership(floorId)
        
        // Check if floor has rooms
        val roomCount = roomRepository.countByFloorFloorId(floorId)
        if (roomCount > 0) {
            throw IllegalArgumentException("Cannot delete floor with $roomCount rooms. Delete or move rooms first.")
        }
        
        floorRepository.delete(floor)
    }

    override fun getFloorsByHouse(houseId: UUID): List<FloorResponse> {
        return floorRepository.findAllByHouseHouseId(houseId)
            .sortedBy { it.floorNumber }
            .map { it.toResponse() }
    }

    override fun getMyFloors(): List<FloorResponse> {
        val ownerId = currentUserService.getCurrentUserId()
        return floorRepository.findAllByHouseOwnerUserId(ownerId).map { it.toResponse() }
    }
}

