package com.example.myapplication.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface SecureApiService {
    @POST("/api/receipts")
    suspend fun addReceipt(@Body data: ReceiptData): Response<Receipt>

    @GET("/api/receipts")
    suspend fun getReceipts(): Response<List<Receipt>>

    @GET("/api/receipts/userInfo")
    suspend fun getUserInfo(): Response<UserInfoResponse>

    @POST("/api/receipts/sendError")
    suspend fun reportError(@Body request: ErrorReportRequest): Response<Void>

    @POST("logout")
    suspend fun logout(): Response<Unit>
}