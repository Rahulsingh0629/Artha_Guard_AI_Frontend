package com.rslab.arthaguardai.network

import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val full_name: String, val username: String, val email: String, val password: String, val phone_number: String)
data class LoginResponse(val access_token: String, val token_type: String, val email: String, val id: String)
data class RegisterResponse(val status: String, val message: String, val email: String)

data class ChatRequest(val message: String)
data class ChatResponse(val response: String)

data class MarketStatusResponse(
    val status: String,
    val reason: String? = null
)

data class MarketIndexItem(
    val name: String? = null,
    val price: Double? = null,
    val change: Double? = null,
    val percent: Double? = null
)

typealias MarketIndicesResponse = Map<String, MarketIndexItem>

data class StockItem(
    val symbol: String,
    val name: String? = null,
    val price: Double? = null,
    val change: Double? = null,
    val percent: Double? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val previousClose: Double? = null,
    val logo: String? = null
)

data class StockResponse(
    val count: Int = 0,
    val stocks: List<StockItem> = emptyList()
)

data class TopMoverItem(
    val symbol: String,
    val percent: Double? = null,
    val logo: String? = null
)

data class TopMoversResponse(
    val gainers: List<TopMoverItem> = emptyList(),
    val losers: List<TopMoverItem> = emptyList()
)

data class InstrumentDetailResponse(
    val symbol: String,
    val name: String? = null,
    val instrumentType: String? = null,
    val price: Double? = null,
    val change: Double? = null,
    val percent: Double? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val previousClose: Double? = null,
    val logo: String? = null,
    val source: String? = null
)

data class InstrumentHistoryPointResponse(
    val timestamp: String? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val close: Double? = null,
    val volume: Double? = null
)

data class InstrumentHistorySummaryResponse(
    val count: Int = 0,
    val firstClose: Double? = null,
    val lastClose: Double? = null,
    val highClose: Double? = null,
    val lowClose: Double? = null
)

data class InstrumentHistoryResponse(
    val symbol: String,
    val name: String? = null,
    val instrumentType: String? = null,
    val range: String? = null,
    val interval: String? = null,
    val source: String? = null,
    val points: List<InstrumentHistoryPointResponse> = emptyList(),
    val summary: InstrumentHistorySummaryResponse? = null
)

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<RegisterResponse>

    @POST("auth/logout")
    fun logout(@Header("Authorization") token: String): Call<Void>

    @GET("market/market-status")
    suspend fun getMarketStatus(): Response<MarketStatusResponse>

    @GET("market/indices")
    suspend fun getMarketIndices(): Response<MarketIndicesResponse>

    @GET("market/stocks")
    suspend fun getStocks(): Response<StockResponse>

    @GET("market/top-movers")
    suspend fun getTopMovers(): Response<TopMoversResponse>

    @GET("market/instrument/{symbol}")
    suspend fun getInstrumentDetails(
        @Path("symbol") symbol: String
    ): Response<InstrumentDetailResponse>

    @GET("market/instrument/{symbol}/history")
    suspend fun getInstrumentHistory(
        @Path("symbol") symbol: String,
        @Query("range") range: String
    ): Response<InstrumentHistoryResponse>

    @POST("advisory/chat")
    suspend fun chatWithAdvisor(
        @Header("Authorization") token: String,
        @Body request: ChatRequest
    ): ChatResponse

}
