package com.rslab.arthaguardai.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.RetrofitInstance
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class StockDetailViewModel(
    symbol: String
) : ViewModel() {

    private val requestedSymbol = symbol.trim()
    private val api = RetrofitInstance.api

    private val _uiState = MutableStateFlow(StockDetailUiState(symbol = requestedSymbol))
    val uiState: StateFlow<StockDetailUiState> = _uiState.asStateFlow()

    private var previousPrice: Double? = null
    private var pollingJob: Job? = null
    private var tickCount = 0

    private val refreshIntervalMs = 4_000L
    private val requestTimeoutMs = 20_000L

    init {
        startPolling()
    }

    fun onRangeSelected(range: String) {
        val normalized = range.uppercase()
        if (normalized == _uiState.value.selectedRange) return

        _uiState.update { it.copy(selectedRange = normalized, isLoading = it.history.isEmpty()) }
        viewModelScope.launch {
            fetchHistory(range = normalized)
        }
    }

    private fun startPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob = viewModelScope.launch {
            while (isActive) {
                fetchDetails()

                val selectedRange = _uiState.value.selectedRange
                val shouldRefreshHistory = tickCount % 3 == 0 || _uiState.value.history.isEmpty()
                if (shouldRefreshHistory) {
                    fetchHistory(range = selectedRange)
                }

                tickCount += 1
                delay(refreshIntervalMs)
            }
        }
    }

    private suspend fun fetchDetails() {
        try {
            val response = withTimeout(requestTimeoutMs) {
                api.getInstrumentDetails(symbol = requestedSymbol)
            }

            if (!response.isSuccessful) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Failed to load details (${response.code()})"
                    )
                }
                return
            }

            val body = response.body()
            if (body == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Invalid detail response."
                    )
                }
                return
            }

            val now = body.price
            val blink = when {
                previousPrice == null || now == null -> BlinkDir.NONE
                now > previousPrice!! -> BlinkDir.UP
                now < previousPrice!! -> BlinkDir.DOWN
                else -> BlinkDir.NONE
            }
            if (now != null) {
                previousPrice = now
            }

            _uiState.update { state ->
                state.copy(
                    symbol = body.symbol,
                    name = body.name ?: body.symbol,
                    instrumentType = body.instrumentType ?: state.instrumentType,
                    price = body.price,
                    change = body.change,
                    percent = body.percent,
                    open = body.open,
                    high = body.high,
                    low = body.low,
                    previousClose = body.previousClose,
                    blink = blink,
                    isLoading = state.history.isEmpty(),
                    error = null
                )
            }
        } catch (_: TimeoutCancellationException) {
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

    private suspend fun fetchHistory(range: String) {
        try {
            val response = withTimeout(requestTimeoutMs) {
                api.getInstrumentHistory(symbol = requestedSymbol, range = range)
            }

            if (!response.isSuccessful) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = "Failed to load chart (${response.code()})"
                    )
                }
                return
            }

            val body = response.body()
            if (body == null) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        error = "Invalid chart response."
                    )
                }
                return
            }

            val closes = body.points.mapNotNull { it.close }
            _uiState.update { state ->
                state.copy(
                    history = closes,
                    selectedRange = range,
                    isLoading = false,
                    error = if (closes.isEmpty()) "No chart data found." else null
                )
            }
        } catch (_: TimeoutCancellationException) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    error = "Chart request timed out. Please try again."
                )
            }
        } catch (e: Exception) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    error = e.localizedMessage ?: "Chart network error"
                )
            }
        }
    }

    override fun onCleared() {
        pollingJob?.cancel()
        super.onCleared()
    }

    class Factory(
        private val symbol: String
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StockDetailViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StockDetailViewModel(symbol) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
        }
    }
}
