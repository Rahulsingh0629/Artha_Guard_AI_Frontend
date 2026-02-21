package com.rslab.arthaguardai.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.MarketIndicesResponse
import com.rslab.arthaguardai.network.MarketStatusResponse
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.network.StockResponse
import com.rslab.arthaguardai.network.TopMoversResponse
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import retrofit2.Response
import java.util.Locale

class HomeViewModel : ViewModel() {
    private data class HomeResponses(
        val statusRes: Response<MarketStatusResponse>,
        val indicesRes: Response<MarketIndicesResponse>,
        val stocksRes: Response<StockResponse>,
        val moversRes: Response<TopMoversResponse>
    )

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private var pollingJob: Job? = null
    private val refreshIntervalMs = 5_000L
    private val requestTimeoutMs = 20_000L
    private val prevIndexPrice = mutableMapOf<String, Double?>()
    private val prevStockPrice = mutableMapOf<String, Double?>()

    init {
        startPolling()
    }

    fun onSearchChange(value: String) {
        _uiState.update { it.copy(search = value) }
    }

    private fun blinkDir(prev: Double?, now: Double?): BlinkDir {
        if (prev == null || now == null) return BlinkDir.NONE
        return when {
            now > prev -> BlinkDir.UP
            now < prev -> BlinkDir.DOWN
            else -> BlinkDir.NONE
        }
    }

    fun startPolling() {
        if (pollingJob?.isActive == true) return

        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchHomeData()
                delay(refreshIntervalMs)
            }
        }
    }

    private suspend fun fetchHomeData() {
        try {
            val responses = withTimeout(requestTimeoutMs) {
                coroutineScope {
                    val statusDeferred = async { RetrofitInstance.api.getMarketStatus() }
                    val indicesDeferred = async { RetrofitInstance.api.getMarketIndices() }
                    val stocksDeferred = async { RetrofitInstance.api.getStocks() }
                    val moversDeferred = async { RetrofitInstance.api.getTopMovers() }

                    HomeResponses(
                        statusRes = statusDeferred.await(),
                        indicesRes = indicesDeferred.await(),
                        stocksRes = stocksDeferred.await(),
                        moversRes = moversDeferred.await()
                    )
                }
            }
            val statusRes = responses.statusRes
            val indicesRes = responses.indicesRes
            val stocksRes = responses.stocksRes
            val moversRes = responses.moversRes

            if (
                statusRes.isSuccessful &&
                indicesRes.isSuccessful &&
                stocksRes.isSuccessful &&
                moversRes.isSuccessful
            ) {
                val indexList = indicesRes.body().orEmpty().map { (key, item) ->
                    val now = item.price
                    val prev = prevIndexPrice[key]
                    prevIndexPrice[key] = now
                    uiIndex(
                        key = key,
                        name = item.name ?: key,
                        price = now,
                        percent = item.percent,
                        blink = blinkDir(prev, now)
                    )
                }.sortedByDescending { kotlin.math.abs(it.percent ?: 0.0) }

                val stocks = stocksRes.body()?.stocks.orEmpty().map { stock ->
                    val now = stock.price
                    val prev = prevStockPrice[stock.symbol]
                    prevStockPrice[stock.symbol] = now
                    uiStock(
                        symbol = stock.symbol,
                        name = stock.name ?: stock.symbol,
                        price = now,
                        percent = stock.percent,
                        blink = blinkDir(prev, now)
                    )
                }.sortedByDescending { kotlin.math.abs(it.percent ?: 0.0) }

                val stockBySymbol = stocks.associateBy { it.symbol.uppercase(Locale.ROOT) }

                val gainers = moversRes.body()?.gainers.orEmpty().map { mover ->
                    val stock = stockBySymbol[mover.symbol.uppercase(Locale.ROOT)]
                    uiStock(
                        symbol = mover.symbol,
                        name = stock?.name ?: mover.symbol,
                        price = stock?.price,
                        percent = mover.percent,
                        blink = stock?.blink ?: BlinkDir.NONE
                    )
                }.sortedByDescending { it.percent ?: 0.0 }

                val losers = moversRes.body()?.losers.orEmpty().map { mover ->
                    val stock = stockBySymbol[mover.symbol.uppercase(Locale.ROOT)]
                    uiStock(
                        symbol = mover.symbol,
                        name = stock?.name ?: mover.symbol,
                        price = stock?.price,
                        percent = mover.percent,
                        blink = stock?.blink ?: BlinkDir.NONE
                    )
                }.sortedBy { it.percent ?: 0.0 }

                val sortedNifty = stocks.sortedByDescending { it.percent ?: 0.0 }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        marketStatus = statusRes.body(),
                        indices = indexList,
                        gainers = gainers,
                        losers = losers,
                        stocks = stocks,
                        nifty50 = sortedNifty,
                        error = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Unable to load market data (${statusRes.code()}/${indicesRes.code()}/${stocksRes.code()}/${moversRes.code()})"
                    )
                }
            }
        } catch (e: TimeoutCancellationException) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = "Request timed out. Please try again."
                )
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Network error"
                )
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }
}
