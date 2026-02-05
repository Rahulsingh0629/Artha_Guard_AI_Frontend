package com.rslab.arthaguardai.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class StockDetailViewModel(
    private val symbol: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(StockDetailUiState())
    val uiState: StateFlow<StockDetailUiState> = _uiState

    init {
        loadStockDetail()
    }

    private fun loadStockDetail() {
        viewModelScope.launch {
            try {
                _uiState.value = StockDetailUiState(isLoading = true)

                val response = RetrofitInstance.api.getStockDetail(symbol)

                _uiState.value = StockDetailUiState(
                    isLoading = false,
                    symbol = response.symbol,
                    name = response.name,
                    price = response.price,
                    change = response.change,
                    percent = response.percent,
                    open = response.open,
                    high = response.high,
                    low = response.low,
                    previousClose = response.previousClose
                )

            } catch (e: Exception) {
                _uiState.value = StockDetailUiState(
                    isLoading = false,
                    error = e.message ?: "Failed to load stock"
                )
            }
        }
    }
}
