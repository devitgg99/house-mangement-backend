package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.time.LocalDateTime
import java.util.*

@Entity
@Table(
    name = "user_follow",
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["follower_id", "following_id"])
    ]
)
data class UserFollow(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "follow_id")
    val followId: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id", nullable = false)
    val follower: User,  // The user who is following

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id", nullable = false)
    val following: User,  // The user being followed

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)

