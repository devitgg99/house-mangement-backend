package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.request.CreateUtilityRequest
import com.example.pvhcenima_api.model.response.UtilityResponse
import java.time.LocalDate
import java.util.*

interface UtilityService {
    // Create utility record at end of month (auto-fetches oldWater from previous record)
    fun createUtility(request: CreateUtilityRequest): UtilityResponse
    
    // Get utility by ID
    fun getUtilityById(utilityId: UUID): UtilityResponse
    
    // Mark utility as paid/unpaid
    fun markPaid(utilityId: UUID, isPay: Boolean): UtilityResponse
    
    // Delete utility
    fun deleteUtility(utilityId: UUID)
    
    // Get all utilities for my rooms (house owner)
    fun getMyUtilities(): List<UtilityResponse>
    
    // Get utilities by room
    fun getUtilitiesByRoom(roomId: UUID): List<UtilityResponse>
    
    // Get utilities by house (optionally filtered by month)
    fun getUtilitiesByHouse(houseId: UUID, month: LocalDate? = null): List<UtilityResponse>
    
    // Get unpaid utilities for a room
    fun getUnpaidUtilities(roomId: UUID): List<UtilityResponse>
    
    // Get utilities by month
    fun getUtilitiesByMonth(month: LocalDate): List<UtilityResponse>
    
    // Get latest utility for a room (to see current water reading)
    fun getLatestUtility(roomId: UUID): UtilityResponse?
}

