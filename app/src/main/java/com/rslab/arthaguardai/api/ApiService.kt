package com.rslab.arthaguardai.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

/* ---------------- REQUEST MODELS ---------------- */

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val full_name: String,
    val username: String,
    val email: String,
    val password: String,
    val phone_number: String
)

/* ---------------- RESPONSE MODELS ---------------- */

data class LoginResponse(
    val access_token: String,
    val token_type: String,
    val email: String,
    val id: String
)

data class RegisterResponse(
    val status: String,
    val message: String,
    val email: String
)


/* ---------------- API SERVICE ---------------- */

interface ApiService {

    @POST("auth/login")
    fun login(
        @Body request: LoginRequest
    ): Call<LoginResponse>

    @POST("auth/register")
    fun register(
        @Body request: RegisterRequest
    ): Call<RegisterResponse>

}
