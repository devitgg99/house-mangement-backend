package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.Room
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface RoomRepository : JpaRepository<Room, UUID> {
    // Find all rooms by house
    fun findAllByHouseHouseId(houseId: UUID): List<Room>
    
    // Find all rooms by house owner (for house owner to see all their rooms)
    fun findAllByHouseOwnerUserId(ownerId: UUID): List<Room>
    
    // Find available rooms (no renter) in a house
    fun findAllByHouseHouseIdAndRenterIsNull(houseId: UUID): List<Room>
    
    // Find rooms rented by a user
    fun findAllByRenterUserId(renterId: UUID): List<Room>
}