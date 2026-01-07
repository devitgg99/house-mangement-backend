package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "floor")
data class Floor(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "floor_id")
    val floorId: UUID? = null,

    @Column(name = "floor_number", nullable = false)
    val floorNumber: Int,

    @Column(name = "floor_name")
    val floorName: String? = null,  // Optional: "Ground Floor", "Rooftop", etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    val house: House
)

