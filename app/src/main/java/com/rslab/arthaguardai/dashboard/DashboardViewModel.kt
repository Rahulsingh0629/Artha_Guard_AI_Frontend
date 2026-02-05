package com.rslab.arthaguardai.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboard(initial = true)
        startAutoRefresh()
    }

    // 🔄 Manual refresh (Pull-to-refresh)
    fun refresh() {
        loadDashboard(isRefresh = true)
    }

    private fun loadDashboard(
        initial: Boolean = false,
        isRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isLoading = initial,
                    isRefreshing = isRefresh,
                    error = null
                )

                val marketStatus = RetrofitInstance.api.getMarketStatus()
                val indices = RetrofitInstance.api.getIndices()
                val stocksResponse = RetrofitInstance.api.getStocks()
                val movers = RetrofitInstance.api.getTopMovers()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,

                    marketStatus = marketStatus,

                    nifty = indices.nifty,
                    sensex = indices.sensex,
                    bankNifty = indices.bankNifty,

                    topGainers = movers.gainers,
                    topLosers = movers.losers,

                    stocks = stocksResponse.stocks
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isRefreshing = false,
                    error = e.message ?: "Something went wrong"
                )
            }
        }
    }

    // ⏱ Auto refresh every 30 seconds
    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(30_000)
                loadDashboard()
            }
        }
    }
}
