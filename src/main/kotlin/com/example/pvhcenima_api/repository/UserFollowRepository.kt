package com.example.pvhcenima_api.repository

import com.example.pvhcenima_api.model.entity.UserFollow
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserFollowRepository : JpaRepository<UserFollow, UUID> {
    
    // Check if user A follows user B
    fun existsByFollowerUserIdAndFollowingUserId(followerId: UUID, followingId: UUID): Boolean
    
    // Find the follow relationship (for unfollowing)
    fun findByFollowerUserIdAndFollowingUserId(followerId: UUID, followingId: UUID): UserFollow?
    
    // Get all users that I'm following
    fun findAllByFollowerUserId(followerId: UUID): List<UserFollow>
    
    // Get all users following me
    fun findAllByFollowingUserId(followingId: UUID): List<UserFollow>
    
    // Count how many users I'm following
    fun countByFollowerUserId(followerId: UUID): Long
    
    // Count how many followers I have
    fun countByFollowingUserId(followingId: UUID): Long
}

