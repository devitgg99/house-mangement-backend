package com.example.pvhcenima_api.model.entity

import jakarta.persistence.*
import java.util.*

@Entity
@Table(name = "app_user")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    val userId: UUID? = null,

    @Column(name = "full_name", nullable = false)
    val fullName: String,

    val password: String,

    @Column(unique = true)  // Email must be unique
    val email: String?,

    @Column(name = "phone_number", unique = true)  // Phone must be unique
    val phoneNumber: String?,

    @Enumerated(EnumType.STRING)
    val role: Role,
    @Column(name = "profile_image")
    val profileImage: String? = null,
)


