package com.example.pvhcenima_api.exception

import com.example.pvhcenima_api.model.response.BaseResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class)
    fun handleBadCredentials(ex: BadCredentialsException): ResponseEntity<BaseResponse<Unit>> =
        ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(BaseResponse.error("Invalid username or password"))

    @ExceptionHandler(UsernameNotFoundException::class)
    fun handleUserNotFound(ex: UsernameNotFoundException): ResponseEntity<BaseResponse<Unit>> =
        ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(BaseResponse.error(ex.message ?: "User not found"))

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(ex: MethodArgumentNotValidException): ResponseEntity<BaseResponse<Unit>> {
        val errors = ex.bindingResult.fieldErrors.map { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.errors(errors))
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(ex: IllegalArgumentException): ResponseEntity<BaseResponse<Unit>> =
        ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(BaseResponse.error(ex.message ?: "Invalid request"))

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception): ResponseEntity<BaseResponse<Unit>> {
        ex.printStackTrace()  // Log the actual error
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(BaseResponse.error(ex.message ?: "An unexpected error occurred"))
    }
}