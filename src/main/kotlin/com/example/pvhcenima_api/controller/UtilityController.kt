package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.request.CreateUtilityRequest
import com.example.pvhcenima_api.model.request.MarkPaidRequest
import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.model.response.UtilityResponse
import com.example.pvhcenima_api.service.UtilityPdfService
import com.example.pvhcenima_api.service.UtilityService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@RestController
@RequestMapping("/api/v1/utility")
@SecurityRequirement(name = "Bearer Authentication")
class UtilityController(
    private val utilityService: UtilityService,
    private val utilityPdfService: UtilityPdfService
) {

    @PostMapping
    @Operation(summary = "Create utility record at end of month (oldWater auto-fetched from previous record)")
    fun createUtility(@RequestBody request: CreateUtilityRequest): BaseResponse<UtilityResponse> {
        return BaseResponse.success(utilityService.createUtility(request), "Utility created successfully")
    }

    @GetMapping("/{utilityId}")
    @Operation(summary = "Get utility by ID")
    fun getUtilityById(@PathVariable utilityId: UUID): BaseResponse<UtilityResponse> {
        return BaseResponse.success(utilityService.getUtilityById(utilityId), "Utility retrieved successfully")
    }

    @PatchMapping("/{utilityId}/pay")
    @Operation(summary = "Mark utility as paid/unpaid")
    fun markPaid(
        @PathVariable utilityId: UUID,
        @RequestBody request: MarkPaidRequest
    ): BaseResponse<UtilityResponse> {
        val message = if (request.paid) "Marked as paid" else "Marked as unpaid"
        return BaseResponse.success(utilityService.markPaid(utilityId, request.paid), message)
    }

    @DeleteMapping("/{utilityId}")
    @Operation(summary = "Delete utility record")
    fun deleteUtility(@PathVariable utilityId: UUID): BaseResponse<Unit> {
        utilityService.deleteUtility(utilityId)
        return BaseResponse.ok("Utility deleted successfully")
    }

    // ==================== Queries ====================

    @GetMapping("/my-utilities")
    @Operation(summary = "Get all utilities for my rooms")
    fun getMyUtilities(): BaseResponse<List<UtilityResponse>> {
        return BaseResponse.success(utilityService.getMyUtilities(), "Utilities retrieved successfully")
    }

    @GetMapping("/room/{roomId}")
    @Operation(summary = "Get utilities by room")
    fun getUtilitiesByRoom(@PathVariable roomId: UUID): BaseResponse<List<UtilityResponse>> {
        return BaseResponse.success(utilityService.getUtilitiesByRoom(roomId), "Utilities retrieved successfully")
    }

    @GetMapping("/room/{roomId}/latest")
    @Operation(summary = "Get latest utility for a room (to see current water reading)")
    fun getLatestUtility(@PathVariable roomId: UUID): BaseResponse<UtilityResponse?> {
        return BaseResponse.success(utilityService.getLatestUtility(roomId), "Latest utility retrieved")
    }

    @GetMapping("/house/{houseId}")
    @Operation(summary = "Get utilities by house (optional month filter)")
    fun getUtilitiesByHouse(
        @PathVariable houseId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate?
    ): BaseResponse<List<UtilityResponse>> {
        return BaseResponse.success(utilityService.getUtilitiesByHouse(houseId, month), "Utilities retrieved successfully")
    }

    @GetMapping("/room/{roomId}/unpaid")
    @Operation(summary = "Get unpaid utilities for a room")
    fun getUnpaidUtilities(@PathVariable roomId: UUID): BaseResponse<List<UtilityResponse>> {
        return BaseResponse.success(utilityService.getUnpaidUtilities(roomId), "Unpaid utilities retrieved successfully")
    }

    @GetMapping("/month/{month}")
    @Operation(summary = "Get utilities by month (format: yyyy-MM-dd)")
    fun getUtilitiesByMonth(
        @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate
    ): BaseResponse<List<UtilityResponse>> {
        return BaseResponse.success(utilityService.getUtilitiesByMonth(month), "Utilities retrieved successfully")
    }

    // ==================== PDF Export ====================

    @GetMapping("/house/{houseId}/pdf")
    @Operation(summary = "Download utility report as PDF (optional month filter)")
    fun downloadUtilityPdf(
        @PathVariable houseId: UUID,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) month: LocalDate?
    ): ResponseEntity<ByteArray> {
        val pdfBytes = utilityPdfService.generateUtilityReport(houseId, month)
        
        val filename = if (month != null) {
            "utility-report-${month.format(DateTimeFormatter.ofPattern("yyyy-MM"))}.pdf"
        } else {
            "utility-report-all.pdf"
        }
        
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=$filename")
            .contentType(MediaType.APPLICATION_PDF)
            .body(pdfBytes)
    }
}

