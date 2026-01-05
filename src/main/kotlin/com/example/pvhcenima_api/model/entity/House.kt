package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "house")
data class House(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "house_id")
    val houseId: UUID? = null,
    @Column(name = "house_name", nullable = false)
    val houseName: String,

    @Column(name = "house_address", nullable = false)
    val houseAddress: String,

    @Column(name = "house_image")
    val houseImage: String? = null,
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User
)