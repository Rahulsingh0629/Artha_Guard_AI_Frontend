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
                delay(10_000) // every 10 seconds
            }
        }
    }

    private suspend fun fetchMarketData() {
        try {
            val response = api.getIndices()

            val niftyQuote = response.nifty.quote
            val sensexQuote = response.sensex.quote

            _uiState.update {
                it.copy(
                    loading = false,

                    niftyValue = niftyQuote.price,
                    niftyChange = niftyQuote.changePercent,
                    niftyPositive = !niftyQuote.change.startsWith("-"),

                    sensexValue = sensexQuote.price,
                    sensexChange = sensexQuote.changePercent,
                    sensexPositive = !sensexQuote.change.startsWith("-"),

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
