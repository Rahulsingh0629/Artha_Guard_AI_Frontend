package com.rslab.arthaguardai.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val full_name: String, val username: String, val email: String, val password: String, val phone_number: String)
data class LoginResponse(val access_token: String, val token_type: String, val email: String, val id: String)
data class RegisterResponse(val status: String, val message: String, val email: String)

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>
    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    @POST("advisory/chat")
    suspend fun chatWithAdvisor(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse

}