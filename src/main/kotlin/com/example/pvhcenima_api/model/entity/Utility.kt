package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Entity
@Table(name = "utility")
data class Utility(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "utility_id")
    val utilityId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    val room: Room,

    @Column(name = "is_pay", nullable = false)
    val isPay: Boolean = false,

    @Column(name = "old_water")
    val oldWater: Double,

    @Column(name = "new_water")
    val newWater: Double,

    @Column(name = "room_cost", precision = 10, scale = 2)
    val roomCost: BigDecimal,

    @Column(name = "water_cost", precision = 10, scale = 2)
    val waterCost: BigDecimal,

    @Column(name = "total_cost", precision = 10, scale = 2)
    val totalCost: BigDecimal,

    @Column(name = "month", nullable = false)
    val month: LocalDate
)
