package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.request.HouseRequest
import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.HouseResponse
import com.example.pvhcenima_api.service.HouseService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/house")
@SecurityRequirement(name = "Bearer Authentication")
class HouseController(
    private val houseService: HouseService
) {
    
    // Create
    @PostMapping
    fun addHouse(@RequestBody houseRequest: HouseRequest): BaseResponse<HouseResponse> {
        return BaseResponse.success(houseService.addHouse(houseRequest), "House added successfully")
    }

    // Read all (my houses)
    @GetMapping
    fun getAllHouses(): BaseResponse<List<HouseResponse>> {
        return BaseResponse.success(houseService.getHouses(), "Houses retrieved successfully")
    }

    // Read by ID
    @GetMapping("/{houseId}")
    fun getHouseById(@PathVariable houseId: UUID): BaseResponse<HouseResponse> {
        return BaseResponse.success(houseService.getHouseById(houseId), "House retrieved successfully")
    }

    // Update
    @PutMapping("/{houseId}")
    fun updateHouse(
        @PathVariable houseId: UUID,
        @RequestBody houseRequest: HouseRequest
    ): BaseResponse<HouseResponse> {
        return BaseResponse.success(houseService.updateHouse(houseId, houseRequest), "House updated successfully")
    }

    // Delete
    @DeleteMapping("/{houseId}")
    fun deleteHouse(@PathVariable houseId: UUID): BaseResponse<Unit> {
        houseService.deleteHouse(houseId)
        return BaseResponse.ok("House deleted successfully")
    }
}