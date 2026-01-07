package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RoomRepository : JpaRepository<Room, UUID> {
    // Find all rooms by floor
    fun findAllByFloorFloorId(floorId: UUID): List<Room>
    
    // Find all rooms by house (through floor)
    fun findAllByFloorHouseHouseId(houseId: UUID): List<Room>
    
    // Find all rooms by house owner (for house owner to see all their rooms)
    fun findAllByFloorHouseOwnerUserId(ownerId: UUID): List<Room>
    
    // Find available rooms (no renter) in a house
    fun findAllByFloorHouseHouseIdAndRenterIsNull(houseId: UUID): List<Room>
    
    // Find available rooms (no renter) on a floor
    fun findAllByFloorFloorIdAndRenterIsNull(floorId: UUID): List<Room>
    
    // Find rooms rented by a user
    fun findAllByRenterUserId(renterId: UUID): List<Room>
    
    // Count rooms in a house
    fun countByFloorHouseHouseId(houseId: UUID): Long
    
    // Count rooms on a floor
    fun countByFloorFloorId(floorId: UUID): Long
}