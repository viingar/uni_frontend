package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("register")
    suspend fun registerUser(@Body request: AuthRequest): Response<Unit>

    @POST("verify")
    suspend fun verifyEmail(@Body request: VerifyRequest): Response<Unit>

    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>

    @POST("checkEmailResetCode")
    suspend fun checkEmail(@Body request: EmailRequest): Response<Unit>

    @POST("validateResetCode")
    suspend fun validateCode(@Body request: CodeResetRequest): Response<Unit>

    @POST("resetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>
}