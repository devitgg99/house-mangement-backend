package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "room")
data class Room(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "room_id")
    val roomId: UUID? = null,

    @Column(name = "room_name", nullable = false)
    val roomName: String,

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    val price: BigDecimal = BigDecimal.ZERO,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "floor_id", nullable = false)
    val floor: Floor,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id")
    val renter: User? = null,

    @ElementCollection
    @CollectionTable(
        name = "room_images",
        joinColumns = [JoinColumn(name = "room_id")]
    )
    @Column(name = "image_url")
    val images: List<String> = emptyList()
)
