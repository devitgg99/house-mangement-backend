package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.request.FloorRequest
import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.FloorResponse
import com.example.pvhcenima_api.service.FloorService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/floor")
@SecurityRequirement(name = "Bearer Authentication")
class FloorController(
    private val floorService: FloorService
) {

    @PostMapping
    @Operation(summary = "Add a new floor to a house")
    fun addFloor(@RequestBody request: FloorRequest): BaseResponse<FloorResponse> {
        return BaseResponse.success(floorService.addFloor(request), "Floor added successfully")
    }

    @GetMapping("/{floorId}")
    @Operation(summary = "Get floor by ID")
    fun getFloorById(@PathVariable floorId: UUID): BaseResponse<FloorResponse> {
        return BaseResponse.success(floorService.getFloorById(floorId), "Floor retrieved successfully")
    }

    @PutMapping("/{floorId}")
    @Operation(summary = "Update floor")
    fun updateFloor(
        @PathVariable floorId: UUID,
        @RequestBody request: FloorRequest
    ): BaseResponse<FloorResponse> {
        return BaseResponse.success(floorService.updateFloor(floorId, request), "Floor updated successfully")
    }

    @DeleteMapping("/{floorId}")
    @Operation(summary = "Delete floor (must have no rooms)")
    fun deleteFloor(@PathVariable floorId: UUID): BaseResponse<Unit> {
        floorService.deleteFloor(floorId)
        return BaseResponse.ok("Floor deleted successfully")
    }

    @GetMapping("/house/{houseId}")
    @Operation(summary = "Get all floors in a house")
    fun getFloorsByHouse(@PathVariable houseId: UUID): BaseResponse<List<FloorResponse>> {
        return BaseResponse.success(floorService.getFloorsByHouse(houseId), "Floors retrieved successfully")
    }

    @GetMapping("/my-floors")
    @Operation(summary = "Get all floors in my houses")
    fun getMyFloors(): BaseResponse<List<FloorResponse>> {
        return BaseResponse.success(floorService.getMyFloors(), "Floors retrieved successfully")
    }
}

