package com.example.pvhcenima_api.model.response

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

data class UtilityResponse(
    val utilityId: UUID?,
    val roomId: UUID?,
    val roomName: String,
    val floorName: String,
    val houseName: String,
    @get:JsonProperty("isPay")
    val paid: Boolean,
    val oldWater: Double,
    val newWater: Double,
    val waterUsage: Double,
    val roomCost: BigDecimal,
    val waterCost: BigDecimal,
    val totalCost: BigDecimal,
    val month: LocalDate
)

