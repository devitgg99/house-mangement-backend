package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.FollowResponse
import com.example.pvhcenima_api.model.response.FollowStats
import com.example.pvhcenima_api.service.FollowService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/follow")
@SecurityRequirement(name = "Bearer Authentication")
class FollowController(
    private val followService: FollowService
) {

    @PostMapping("/{userId}")
    @Operation(summary = "Follow a user")
    fun follow(@PathVariable userId: UUID): BaseResponse<FollowResponse> {
        return BaseResponse.success(followService.follow(userId), "Successfully followed user")
    }

    @DeleteMapping("/{userId}")
    @Operation(summary = "Unfollow a user")
    fun unfollow(@PathVariable userId: UUID): BaseResponse<Unit> {
        followService.unfollow(userId)
        return BaseResponse.ok("Successfully unfollowed user")
    }

    @GetMapping("/following")
    @Operation(summary = "Get all users I'm following")
    fun getFollowing(): BaseResponse<List<FollowResponse>> {
        return BaseResponse.success(followService.getFollowing(), "Following list retrieved")
    }

    @GetMapping("/followers")
    @Operation(summary = "Get all my followers")
    fun getFollowers(): BaseResponse<List<FollowResponse>> {
        return BaseResponse.success(followService.getFollowers(), "Followers list retrieved")
    }

    @GetMapping("/check/{userId}")
    @Operation(summary = "Check if I'm following a specific user")
    fun isFollowing(@PathVariable userId: UUID): BaseResponse<Boolean> {
        return BaseResponse.success(followService.isFollowing(userId), "Follow status checked")
    }

    @GetMapping("/stats")
    @Operation(summary = "Get my follow statistics")
    fun getMyFollowStats(): BaseResponse<FollowStats> {
        return BaseResponse.success(followService.getMyFollowStats(), "Stats retrieved")
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Get follow statistics for any user")
    fun getFollowStats(@PathVariable userId: UUID): BaseResponse<FollowStats> {
        return BaseResponse.success(followService.getFollowStats(userId), "Stats retrieved")
    }
}

