package com.rslab.arthaguardai.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.api.RetrofitInstance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val api = RetrofitInstance.api

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        startMarketUpdates()
    }

    private fun startMarketUpdates() {
        viewModelScope.launch {
            while (true) {
                fetchMarketData()
                delay(60_000) // 1 minute (safe for Alpha Vantage)
            }
        }
    }

    private suspend fun fetchMarketData() {
        try {
            val response = api.getIndices()

            val nifty = response.nifty50
            val sensex = response.sensex

            _uiState.update {
                it.copy(
                    loading = false,

                    niftyValue = if (nifty.available) nifty.price else "--",
                    niftyChange = if (nifty.available) nifty.changePercent else "--",
                    niftyPositive = nifty.positive,

                    sensexValue = if (sensex.available) sensex.price else "--",
                    sensexChange = if (sensex.available) sensex.changePercent else "--",
                    sensexPositive = sensex.positive,

                    error = null
                )
            }

        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    loading = false,
                    error = "Market data unavailable"
                )
            }
        }
    }
}
