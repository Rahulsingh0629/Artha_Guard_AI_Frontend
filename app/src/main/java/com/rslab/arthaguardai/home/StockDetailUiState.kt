package com.rslab.arthaguardai.home

data class StockDetailUiState(
    val symbol: String,
    val name: String = symbol,
    val instrumentType: String = "stock",
    val price: Double? = null,
    val percent: Double? = null,
    val change: Double? = null,
    val open: Double? = null,
    val high: Double? = null,
    val low: Double? = null,
    val previousClose: Double? = null,
    val history: List<Double> = emptyList(),
    val selectedRange: String = "1D",
    val blink: BlinkDir = BlinkDir.NONE,
    val isLoading: Boolean = true,
    val error: String? = null
)
