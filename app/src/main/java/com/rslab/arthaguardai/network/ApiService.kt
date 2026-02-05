package com.rslab.arthaguardai.network

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// ... Keep your Login/Register Request classes here ...
data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val full_name: String, val username: String, val email: String, val password: String, val phone_number: String)
data class LoginResponse(val access_token: String, val token_type: String, val email: String, val id: String)
data class RegisterResponse(val status: String, val message: String, val email: String)

interface ApiService {

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<RegisterResponse>

    // --- 📊 NEW MARKET ENDPOINTS ---

    @GET("market/market-status")
    suspend fun getMarketStatus(): MarketStatusResponse

    @GET("market/indices")
    suspend fun getIndices(): IndicesResponse

    @GET("market/top-movers")
    suspend fun getTopMovers(): TopMoversResponse

    @GET("market/stocks")
    suspend fun getStocks(): StocksResponse

    @GET("market/stock/{symbol}")
    suspend fun getStockDetail(@Path("symbol") symbol: String): StockDetailResponse
}