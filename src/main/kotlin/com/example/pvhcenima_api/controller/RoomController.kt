package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.request.RoomRequest
import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.RoomResponse
import com.example.pvhcenima_api.service.RoomService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/room")
@SecurityRequirement(name = "Bearer Authentication")
class RoomController(
    private val roomService: RoomService
) {
    @PostMapping
    @Operation(summary = "Add a new room to a house")
    fun addRoom(@RequestBody request: RoomRequest): BaseResponse<RoomResponse> {
        return BaseResponse.success(roomService.addRoom(request), "Room added successfully")
    }

    @GetMapping("/{roomId}")
    @Operation(summary = "Get room by ID")
    fun getRoomById(@PathVariable roomId: UUID): BaseResponse<RoomResponse> {
        return BaseResponse.success(roomService.getRoomById(roomId), "Room retrieved successfully")
    }

    @PutMapping("/{roomId}")
    @Operation(summary = "Update room")
    fun updateRoom(
        @PathVariable roomId: UUID,
        @RequestBody request: RoomRequest
    ): BaseResponse<RoomResponse> {
        return BaseResponse.success(roomService.updateRoom(roomId, request), "Room updated successfully")
    }

    @DeleteMapping("/{roomId}")
    @Operation(summary = "Delete room")
    fun deleteRoom(@PathVariable roomId: UUID): BaseResponse<Unit> {
        roomService.deleteRoom(roomId)
        return BaseResponse.ok("Room deleted successfully")
    }


    @GetMapping("/my-rooms")
    @Operation(summary = "Get all rooms in my houses (for house owners)")
    fun getMyRooms(): BaseResponse<List<RoomResponse>> {
        return BaseResponse.success(roomService.getMyRooms(), "Rooms retrieved successfully")
    }

    @GetMapping("/house/{houseId}")
    @Operation(summary = "Get all rooms in a specific house")
    fun getRoomsByHouse(@PathVariable houseId: UUID): BaseResponse<List<RoomResponse>> {
        return BaseResponse.success(roomService.getRoomsByHouse(houseId), "Rooms retrieved successfully")
    }

    @GetMapping("/house/{houseId}/available")
    @Operation(summary = "Get available rooms in a house (no renter)")
    fun getAvailableRooms(@PathVariable houseId: UUID): BaseResponse<List<RoomResponse>> {
        return BaseResponse.success(roomService.getAvailableRooms(houseId), "Available rooms retrieved successfully")
    }

    @GetMapping("/my-rented")
    @Operation(summary = "Get rooms I'm renting (for renters)")
    fun getMyRentedRooms(): BaseResponse<List<RoomResponse>> {
        return BaseResponse.success(roomService.getMyRentedRooms(), "Rented rooms retrieved successfully")
    }


    @PostMapping("/{roomId}/assign-renter/{renterId}")
    @Operation(summary = "Assign a renter to a room (house owner only)")
    fun assignRenter(
        @PathVariable roomId: UUID,
        @PathVariable renterId: UUID
    ): BaseResponse<RoomResponse> {
        return BaseResponse.success(roomService.assignRenter(roomId, renterId), "Renter assigned successfully")
    }

    @DeleteMapping("/{roomId}/remove-renter")
    @Operation(summary = "Remove renter from a room (house owner only)")
    fun removeRenter(@PathVariable roomId: UUID): BaseResponse<RoomResponse> {
        return BaseResponse.success(roomService.removeRenter(roomId), "Renter removed successfully")
    }
}