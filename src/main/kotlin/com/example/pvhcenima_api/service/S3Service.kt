package com.example.pvhcenima_api.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.*

@Service
class S3Service(
    private val s3Client: S3Client,

    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String,

    @Value("\${cloud.aws.region.static}")
    private val region: String
) {

    fun uploadImage(file: MultipartFile): String {
        val key = "images/${UUID.randomUUID()}.${file.originalFilename?.substringAfterLast(".") ?: "jpg"}"

        val putRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(file.contentType)
            .build()

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.bytes))

        return "https://$bucketName.s3.$region.amazonaws.com/$key"
    }

    fun deleteImage(imageUrl: String) {
        val key = imageUrl.substringAfter(".amazonaws.com/")

        s3Client.deleteObject(
            DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
        )
    }
}

