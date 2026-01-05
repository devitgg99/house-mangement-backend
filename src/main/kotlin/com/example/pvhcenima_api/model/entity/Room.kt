package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "house_id", nullable = false)
    val house: House,

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
