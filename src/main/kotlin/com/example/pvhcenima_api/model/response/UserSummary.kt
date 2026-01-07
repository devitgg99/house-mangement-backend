package com.example.pvhcenima_api.model.response

import java.time.LocalDateTime
import java.util.*

/**
 * Summary of a user for displaying in follow lists
 */
data class UserSummary(
    val userId: UUID?,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val role: String
)

/**
 * User summary with follow status - for browsing users
 */
data class UserWithFollowStatus(
    val userId: UUID?,
    val fullName: String,
    val email: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val role: String,
    val isFollowing: Boolean
)

/**
 * Response for follow relationship including when it was created
 */
data class FollowResponse(
    val user: UserSummary,
    val followedAt: LocalDateTime
)

/**
 * Response for follow stats
 */
data class FollowStats(
    val followersCount: Long,
    val followingCount: Long
)

