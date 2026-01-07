package com.example.pvhcenima_api.service

import com.example.pvhcenima_api.model.response.FollowResponse
import com.example.pvhcenima_api.model.response.FollowStats
import java.util.*

interface FollowService {
    
    // Follow a user
    fun follow(targetUserId: UUID): FollowResponse
    
    // Unfollow a user
    fun unfollow(targetUserId: UUID)
    
    // Get all users I'm following
    fun getFollowing(): List<FollowResponse>
    
    // Get all my followers
    fun getFollowers(): List<FollowResponse>
    
    // Check if I follow a specific user
    fun isFollowing(targetUserId: UUID): Boolean
    
    // Get follow stats for current user
    fun getMyFollowStats(): FollowStats
    
    // Get follow stats for any user
    fun getFollowStats(userId: UUID): FollowStats
}

