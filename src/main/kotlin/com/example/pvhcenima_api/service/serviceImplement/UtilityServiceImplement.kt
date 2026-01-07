package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.Utility
import com.example.pvhcenima_api.model.request.CreateUtilityRequest
import com.example.pvhcenima_api.model.response.UtilityResponse
import com.example.pvhcenima_api.repository.RoomRepository
import com.example.pvhcenima_api.repository.UtilityRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.UtilityService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Service
class UtilityServiceImplement(
    private val utilityRepository: UtilityRepository,
    private val roomRepository: RoomRepository,
    private val currentUserService: CurrentUserService,

    @Value("\${utility.water-price-per-unit:5000}")
    private val waterPricePerUnit: BigDecimal
) : UtilityService {

    private fun Utility.toResponse() = UtilityResponse(
        utilityId = this.utilityId,
        roomId = this.room.roomId,
        roomName = this.room.roomName,
        houseName = this.room.floor.house.houseName,
        paid = this.isPay,
        oldWater = this.oldWater,
        newWater = this.newWater,
        waterUsage = this.newWater - this.oldWater,
        roomCost = this.roomCost,
        waterCost = this.waterCost,
        totalCost = this.totalCost,
        month = this.month
    )

    private fun checkRoomOwnership(roomId: UUID) {
        val room = roomRepository.findById(roomId)
            .orElseThrow { IllegalArgumentException("Room not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (room.floor.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't own this room")
        }
    }

    private fun checkUtilityOwnership(utilityId: UUID): Utility {
        val utility = utilityRepository.findById(utilityId)
            .orElseThrow { IllegalArgumentException("Utility not found") }

        val currentUserId = currentUserService.getCurrentUserId()
        if (utility.room.floor.house.owner.userId != currentUserId) {
            throw IllegalArgumentException("You don't have permission to manage this utility")
        }
        return utility
    }

    private fun calculateWaterCost(oldWater: Double, newWater: Double): BigDecimal {
        val usage = newWater - oldWater
        return if (usage > 0) {
            BigDecimal.valueOf(usage).multiply(waterPricePerUnit)
        } else {
            BigDecimal.ZERO
        }
    }

    // ==================== CRUD ====================

    override fun createUtility(request: CreateUtilityRequest): UtilityResponse {
        checkRoomOwnership(request.roomId)

        val room = roomRepository.findById(request.roomId)
            .orElseThrow { IllegalArgumentException("Room not found") }

        // Check if utility already exists for this room and month
        val existing = utilityRepository.findByRoomRoomIdAndMonth(request.roomId, request.month)
        if (existing != null) {
            throw IllegalArgumentException("Utility record already exists for this room in ${request.month}")
        }

        // Determine oldWater
        val previousUtility = utilityRepository.findFirstByRoomRoomIdOrderByMonthDesc(request.roomId)
        val oldWater = when {
            // If user provides oldWater AND this is first record → use it
            request.oldWater != null && previousUtility == null -> request.oldWater
            // If user provides oldWater BUT there's previous record → ignore it, use previous
            request.oldWater != null && previousUtility != null -> previousUtility.newWater
            // No oldWater provided → use previous or 0
            else -> previousUtility?.newWater ?: 0.0
        }

        // Validate newWater >= oldWater
        if (request.newWater < oldWater) {
            throw IllegalArgumentException("New water (${request.newWater}) cannot be less than previous reading ($oldWater)")
        }

        // Calculate costs - use room's price
        val roomCost = room.price
        val waterCost = calculateWaterCost(oldWater, request.newWater)
        val totalCost = roomCost.add(waterCost)

        val utility = utilityRepository.save(
            Utility(
                room = room,
                oldWater = oldWater,
                newWater = request.newWater,
                roomCost = roomCost,
                waterCost = waterCost,
                totalCost = totalCost,
                month = request.month
            )
        )
        return utility.toResponse()
    }

    override fun getUtilityById(utilityId: UUID): UtilityResponse {
        val utility = utilityRepository.findById(utilityId)
            .orElseThrow { IllegalArgumentException("Utility not found") }
        return utility.toResponse()
    }

    override fun markPaid(utilityId: UUID, isPay: Boolean): UtilityResponse {
        val utility = checkUtilityOwnership(utilityId)
        print(isPay.toString())
        val updatedUtility = utilityRepository.save(
            Utility(
                utilityId = utility.utilityId,
                room = utility.room,
                isPay = isPay,
                oldWater = utility.oldWater,
                newWater = utility.newWater,
                roomCost = utility.roomCost,
                waterCost = utility.waterCost,
                totalCost = utility.totalCost,
                month = utility.month
            )
        )
        return updatedUtility.toResponse()
    }

    override fun deleteUtility(utilityId: UUID) {
        val utility = checkUtilityOwnership(utilityId)
        utilityRepository.delete(utility)
    }

    // ==================== Queries ====================

    override fun getMyUtilities(): List<UtilityResponse> {
        val ownerId = currentUserService.getCurrentUserId()
        return utilityRepository.findAllByRoomFloorHouseOwnerUserId(ownerId).map { it.toResponse() }
    }

    override fun getUtilitiesByRoom(roomId: UUID): List<UtilityResponse> {
        return utilityRepository.findAllByRoomRoomId(roomId).map { it.toResponse() }
    }

    override fun getUtilitiesByHouse(houseId: UUID, month: LocalDate?): List<UtilityResponse> {
        return if (month != null) {
            utilityRepository.findAllByRoomFloorHouseHouseIdAndMonth(houseId, month).map { it.toResponse() }
        } else {
            utilityRepository.findAllByRoomFloorHouseHouseId(houseId).map { it.toResponse() }
        }
    }

    override fun getUnpaidUtilities(roomId: UUID): List<UtilityResponse> {
        return utilityRepository.findAllByRoomRoomIdAndIsPayFalse(roomId).map { it.toResponse() }
    }

    override fun getUtilitiesByMonth(month: LocalDate): List<UtilityResponse> {
        return utilityRepository.findAllByMonth(month).map { it.toResponse() }
    }

    override fun getLatestUtility(roomId: UUID): UtilityResponse? {
        return utilityRepository.findFirstByRoomRoomIdOrderByMonthDesc(roomId)?.toResponse()
    }
}

