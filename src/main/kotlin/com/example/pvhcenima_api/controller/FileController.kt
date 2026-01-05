package com.example.pvhcenima_api.controller

import com.example.pvhcenima_api.model.response.BaseResponse
import com.example.pvhcenima_api.service.S3Service
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirements
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/file")
@SecurityRequirements
class FileController(
    private val s3Service: S3Service
) {

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload image to S3")
    fun uploadImage(@RequestParam("file") file: MultipartFile): BaseResponse<String> {
        // Validate
        if (file.isEmpty) {
            throw IllegalArgumentException("File is empty")
        }
        if (file.size > 10 * 1024 * 1024) {
            throw IllegalArgumentException("File size exceeds 10MB")
        }
        val allowedTypes = listOf("image/jpeg", "image/png", "image/gif", "image/webp")
        if (file.contentType !in allowedTypes) {
            throw IllegalArgumentException("Only images allowed (JPEG, PNG, GIF, WebP)")
        }

        val url = s3Service.uploadImage(file)
        return BaseResponse.success(url, "Image uploaded successfully")
    }
}

