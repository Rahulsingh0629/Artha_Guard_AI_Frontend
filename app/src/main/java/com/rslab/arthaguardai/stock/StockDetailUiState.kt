package com.rslab.arthaguardai.stock

data class StockDetailUiState(
    val isLoading: Boolean = true,
    val error: String? = null,

    val symbol: String = "",
    val name: String = "",

    val price: Double = 0.0,
    val change: Double = 0.0,
    val percent: Double = 0.0,

    val open: Double = 0.0,
    val high: Double = 0.0,
    val low: Double = 0.0,
    val previousClose: Double = 0.0
)
