package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.House
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface HouseRepository : JpaRepository<House, UUID> {
    // Find by owner's userId (UUID)
    fun findAllByOwnerUserId(ownerId: UUID): List<House>
    
    // Or find by owner entity
    // fun findAllByOwner(owner: User): List<House>
}