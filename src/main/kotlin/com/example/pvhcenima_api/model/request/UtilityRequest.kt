package com.example.pvhcenima_api.model.request

import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

// Create utility record at end of month
data class CreateUtilityRequest(
    val roomId: UUID,
    val newWater: Double,              // Current meter reading
    val roomCost: BigDecimal,
    val month: LocalDate,
    val oldWater: Double? = null       // Optional: only for FIRST record of new room
)
// If oldWater is null → auto-fetch from previous record (or 0 if truly first)
// If oldWater is provided → use it (for new room with existing meter reading)

// Mark as paid
data class MarkPaidRequest(
    @field:io.swagger.v3.oas.annotations.media.Schema(name = "isPay", description = "Payment status", hidden = false)
    @field:com.fasterxml.jackson.annotation.JsonProperty("isPay")
    @param:com.fasterxml.jackson.annotation.JsonProperty("isPay")
    val paid: Boolean = false
)

