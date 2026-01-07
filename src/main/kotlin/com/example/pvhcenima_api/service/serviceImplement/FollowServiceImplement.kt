package com.example.pvhcenima_api.service.serviceImplement

import com.example.pvhcenima_api.model.entity.User
import com.example.pvhcenima_api.model.entity.UserFollow
import com.example.pvhcenima_api.model.response.FollowResponse
import com.example.pvhcenima_api.model.response.FollowStats
import com.example.pvhcenima_api.model.response.UserSummary
import com.example.pvhcenima_api.repository.UserFollowRepository
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.CurrentUserService
import com.example.pvhcenima_api.service.FollowService
import org.springframework.stereotype.Service
import java.util.*

@Service
class FollowServiceImplement(
    private val userFollowRepository: UserFollowRepository,
    private val userRepository: UserRepository,
    private val currentUserService: CurrentUserService
) : FollowService {

    private fun User.toSummary() = UserSummary(
        userId = this.userId,
        fullName = this.fullName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        profileImage = this.profileImage,
        role = this.role.name
    )

    private fun UserFollow.toFollowingResponse() = FollowResponse(
        user = this.following.toSummary(),
        followedAt = this.createdAt
    )

    private fun UserFollow.toFollowerResponse() = FollowResponse(
        user = this.follower.toSummary(),
        followedAt = this.createdAt
    )

    override fun follow(targetUserId: UUID): FollowResponse {
        val currentUserId = currentUserService.getCurrentUserId()
        
        // Can't follow yourself
        if (currentUserId == targetUserId) {
            throw IllegalArgumentException("You cannot follow yourself")
        }
        
        // Check if already following
        if (userFollowRepository.existsByFollowerUserIdAndFollowingUserId(currentUserId, targetUserId)) {
            throw IllegalArgumentException("You are already following this user")
        }
        
        // Get both users
        val follower = userRepository.findById(currentUserId)
            .orElseThrow { IllegalArgumentException("Current user not found") }
        val following = userRepository.findById(targetUserId)
            .orElseThrow { IllegalArgumentException("User to follow not found") }
        
        // Create follow relationship
        val userFollow = userFollowRepository.save(
            UserFollow(
                follower = follower,
                following = following
            )
        )
        
        return userFollow.toFollowingResponse()
    }

    override fun unfollow(targetUserId: UUID) {
        val currentUserId = currentUserService.getCurrentUserId()
        
        // Can't unfollow yourself
        if (currentUserId == targetUserId) {
            throw IllegalArgumentException("You cannot unfollow yourself")
        }
        
        // Find the follow relationship
        val follow = userFollowRepository.findByFollowerUserIdAndFollowingUserId(currentUserId, targetUserId)
            ?: throw IllegalArgumentException("You are not following this user")
        
        userFollowRepository.delete(follow)
    }

    override fun getFollowing(): List<FollowResponse> {
        val currentUserId = currentUserService.getCurrentUserId()
        return userFollowRepository.findAllByFollowerUserId(currentUserId)
            .map { it.toFollowingResponse() }
    }

    override fun getFollowers(): List<FollowResponse> {
        val currentUserId = currentUserService.getCurrentUserId()
        return userFollowRepository.findAllByFollowingUserId(currentUserId)
            .map { it.toFollowerResponse() }
    }

    override fun isFollowing(targetUserId: UUID): Boolean {
        val currentUserId = currentUserService.getCurrentUserId()
        return userFollowRepository.existsByFollowerUserIdAndFollowingUserId(currentUserId, targetUserId)
    }

    override fun getMyFollowStats(): FollowStats {
        val currentUserId = currentUserService.getCurrentUserId()
        return FollowStats(
            followersCount = userFollowRepository.countByFollowingUserId(currentUserId),
            followingCount = userFollowRepository.countByFollowerUserId(currentUserId)
        )
    }

    override fun getFollowStats(userId: UUID): FollowStats {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw IllegalArgumentException("User not found")
        }
        
        return FollowStats(
            followersCount = userFollowRepository.countByFollowingUserId(userId),
            followingCount = userFollowRepository.countByFollowerUserId(userId)
        )
    }
}

