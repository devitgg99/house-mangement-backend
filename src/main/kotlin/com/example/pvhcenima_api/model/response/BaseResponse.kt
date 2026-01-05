package com.example.pvhcenima_api.model.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.Instant

@JsonInclude(JsonInclude.Include.NON_NULL)  // Hide null fields in JSON
data class BaseResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val errors: List<String>? = null,
    val createdAt: Instant = Instant.now()
) {
    companion object {
        // Success with data
        fun <T> success(data: T, message: String? = null): BaseResponse<T> =
            BaseResponse(success = true, message = message, data = data)

        // Success without data (empty body)
        fun ok(message: String? = "Success"): BaseResponse<Unit> =
            BaseResponse(success = true, message = message)

        // Error with message
        fun error(message: String): BaseResponse<Unit> =
            BaseResponse(success = false, message = message)

        // Error with multiple errors (e.g., validation)
        fun errors(errors: List<String>, message: String? = "Validation failed"): BaseResponse<Unit> =
            BaseResponse(success = false, message = message, errors = errors)
    }
}

