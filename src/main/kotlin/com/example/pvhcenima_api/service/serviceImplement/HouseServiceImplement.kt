package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.Floor
import com.example.pvhcenima_api.model.entity.House
import com.example.pvhcenima_api.model.request.HouseRequest
import com.example.pvhcenima_api.model.response.FloorSummary
import com.example.pvhcenima_api.model.response.HouseResponse
import com.example.pvhcenima_api.repository.FloorRepository
import com.example.pvhcenima_api.repository.HouseRepository
import com.example.pvhcenima_api.repository.RoomRepository
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.HouseService
import org.springframework.stereotype.Service
import java.util.*

@Service
class HouseServiceImplement(
    private val houseRepository: HouseRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService,
    private val roomRepository: RoomRepository,
    private val floorRepository: FloorRepository
) : HouseService {

    private fun getCurrentOwner() = userRepository.findById(currentUserService.getCurrentUserId())
        .orElseThrow { IllegalArgumentException("User not found") }

    private fun Floor.toSummary() = FloorSummary(
        floorId = this.floorId,
        floorNumber = this.floorNumber,
        floorName = this.floorName,
        totalRooms = roomRepository.countByFloorFloorId(this.floorId!!)
    )

    private fun House.toResponse(includeFloors: Boolean = false): HouseResponse {
        val floorList = if (includeFloors) {
            floorRepository.findAllByHouseHouseId(this.houseId!!)
                .sortedBy { it.floorNumber }
                .map { it.toSummary() }
        } else {
            emptyList()
        }
        
        return HouseResponse(
            houseId = this.houseId,
            houseName = this.houseName,
            houseAddress = this.houseAddress,
            houseImage = this.houseImage,
            totalFloors = floorRepository.countByHouseHouseId(this.houseId!!),
            totalRooms = roomRepository.countByFloorHouseHouseId(this.houseId!!),
            floors = floorList
        )
    }

    override fun addHouse(house: HouseRequest): HouseResponse {
        val owner = getCurrentOwner()
        val savedHouse = houseRepository.save(
            House(
                houseName = house.houseName,
                houseAddress = house.houseAddress,
                houseImage = house.houseImage,
                owner = owner
            )
        )
        return savedHouse.toResponse()
    }

    override fun getHouses(): List<HouseResponse> {
        val ownerId = currentUserService.getCurrentUserId()
        return houseRepository.findAllByOwnerUserId(ownerId).map { it.toResponse() }
    }

    override fun getHouseById(houseId: UUID): HouseResponse {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }
        
        // Check if current user owns this house
        val currentUserId = currentUserService.getCurrentUserId()
        if (house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to view this house")
        }
        
        return house.toResponse(includeFloors = true)  // Include floors for detail view
    }

    override fun updateHouse(houseId: UUID, request: HouseRequest): HouseResponse {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }
        
        // Check if current user owns this house
        val currentUserId = currentUserService.getCurrentUserId()
        if (house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to update this house")
        }
        
        // Update the house
        val updatedHouse = houseRepository.save(
            house.copy(
                houseName = request.houseName,
                houseAddress = request.houseAddress,
                houseImage = request.houseImage
            )
        )
        return updatedHouse.toResponse()
    }

    override fun deleteHouse(houseId: UUID) {
        val house = houseRepository.findById(houseId)
            .orElseThrow { IllegalArgumentException("House not found") }
        
        // Check if current user owns this house
        val currentUserId = currentUserService.getCurrentUserId()
        if (house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to delete this house")
        }
        
        houseRepository.delete(house)
    }
}