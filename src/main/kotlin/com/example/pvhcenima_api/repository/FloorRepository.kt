package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.Floor
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FloorRepository : JpaRepository<Floor, UUID> {
    // Find all floors in a house
    fun findAllByHouseHouseId(houseId: UUID): List<Floor>
    
    // Find all floors by house owner
    fun findAllByHouseOwnerUserId(ownerId: UUID): List<Floor>
    
    // Count floors in a house
    fun countByHouseHouseId(houseId: UUID): Long
    
    // Check if floor number already exists in house
    fun existsByHouseHouseIdAndFloorNumber(houseId: UUID, floorNumber: Int): Boolean
}

