package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.entity.Role
import com.example.pvhcenima_api.model.entity.User
import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.UserSummary
import com.example.pvhcenima_api.model.response.UserWithFollowStatus
import com.example.pvhcenima_api.repository.UserFollowRepository
import com.example.pvhcenima_api.repository.UserRepository
import com.example.pvhcenima_api.service.CurrentUserService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/user")
@SecurityRequirement(name = "Bearer Authentication")
class UserController(
    private val userRepository: UserRepository,
    private val userFollowRepository: UserFollowRepository,
    private val currentUserService: CurrentUserService
) {

    private fun User.toSummary() = UserSummary(
        userId = this.userId,
        fullName = this.fullName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        profileImage = this.profileImage,
        role = this.role.name
    )

    private fun User.toWithFollowStatus(currentUserId: UUID) = UserWithFollowStatus(
        userId = this.userId,
        fullName = this.fullName,
        email = this.email,
        phoneNumber = this.phoneNumber,
        profileImage = this.profileImage,
        role = this.role.name,
        isFollowing = userFollowRepository.existsByFollowerUserIdAndFollowingUserId(currentUserId, this.userId!!)
    )

    @GetMapping
    @Operation(summary = "Get all users (browse to follow) - includes isFollowing status")
    fun getAllUsers(
        @RequestParam(required = false) role: Role?,
        @RequestParam(required = false) search: String?
    ): BaseResponse<List<UserWithFollowStatus>> {
        val currentUserId = currentUserService.getCurrentUserId()
        
        var users = userRepository.findAll()
            .filter { it.userId != currentUserId }  // Exclude current user
        
        // Filter by role if provided
        if (role != null) {
            users = users.filter { it.role == role }
        }
        
        // Search by name, email, or phone
        if (!search.isNullOrBlank()) {
            val searchLower = search.lowercase()
            users = users.filter {
                it.fullName.lowercase().contains(searchLower) ||
                it.email?.lowercase()?.contains(searchLower) == true ||
                it.phoneNumber?.contains(search) == true
            }
        }
        
        return BaseResponse.success(users.map { it.toWithFollowStatus(currentUserId) }, "Users retrieved successfully")
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get user by ID")
    fun getUserById(@PathVariable userId: UUID): BaseResponse<UserSummary> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        return BaseResponse.success(user.toSummary(), "User retrieved successfully")
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    fun getMyProfile(): BaseResponse<UserSummary> {
        val currentUserId = currentUserService.getCurrentUserId()
        val user = userRepository.findById(currentUserId)
            .orElseThrow { IllegalArgumentException("User not found") }
        return BaseResponse.success(user.toSummary(), "Profile retrieved successfully")
    }

    @GetMapping("/renters")
    @Operation(summary = "Get all renters - includes isFollowing status")
    fun getAllRenters(): BaseResponse<List<UserWithFollowStatus>> {
        val currentUserId = currentUserService.getCurrentUserId()
        val renters = userRepository.findAll()
            .filter { it.role == Role.RENTER && it.userId != currentUserId }
            .map { it.toWithFollowStatus(currentUserId) }
        return BaseResponse.success(renters, "Renters retrieved successfully")
    }

    @GetMapping("/owners")
    @Operation(summary = "Get all house owners - includes isFollowing status")
    fun getAllOwners(): BaseResponse<List<UserWithFollowStatus>> {
        val currentUserId = currentUserService.getCurrentUserId()
        val owners = userRepository.findAll()
            .filter { it.role == Role.HOUSEOWNER && it.userId != currentUserId }
            .map { it.toWithFollowStatus(currentUserId) }
        return BaseResponse.success(owners, "Owners retrieved successfully")
    }
}

