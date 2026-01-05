package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.request.HouseRequest
import com.example.pvhcenima_api.model.response.HouseResponse
import java.util.*

interface HouseService {
    fun addHouse(house: HouseRequest): HouseResponse
    fun getHouses(): List<HouseResponse>
    fun getHouseById(houseId: UUID): HouseResponse
    fun updateHouse(houseId: UUID, house: HouseRequest): HouseResponse
    fun deleteHouse(houseId: UUID)
}