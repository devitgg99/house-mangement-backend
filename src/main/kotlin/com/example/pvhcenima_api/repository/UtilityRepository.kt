package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.Utility
import org.springframework.data.jpa.repository.JpaRepository
import java.time.LocalDate
import java.util.*

interface UtilityRepository : JpaRepository<Utility, UUID> {
    // Find by room
    fun findAllByRoomRoomId(roomId: UUID): List<Utility>
    
    // Find by room and month
    fun findByRoomRoomIdAndMonth(roomId: UUID, month: LocalDate): Utility?
    
    // Find all utilities for rooms owned by a house owner
    fun findAllByRoomHouseOwnerUserId(ownerId: UUID): List<Utility>
    
    // Find unpaid utilities for a room
    fun findAllByRoomRoomIdAndIsPayFalse(roomId: UUID): List<Utility>
    
    // Find all utilities by month
    fun findAllByMonth(month: LocalDate): List<Utility>
    
    // Find utilities by house
    fun findAllByRoomHouseHouseId(houseId: UUID): List<Utility>
    
    // Find latest utility for a room (by month descending)
    fun findFirstByRoomRoomIdOrderByMonthDesc(roomId: UUID): Utility?
}

