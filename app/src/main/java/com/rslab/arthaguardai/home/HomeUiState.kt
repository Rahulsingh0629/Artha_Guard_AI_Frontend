package com.rslab.arthaguardai.home

import com.rslab.arthaguardai.network.MarketStatusResponse

enum class BlinkDir{UP, DOWN, NONE }

data class uiIndex(
    val key: String,
    val name: String,
    val price: Double?,
    val percent: Double?,
    val blink: BlinkDir = BlinkDir.NONE
)

data class uiStock(
    val symbol: String,
    val name: String,
    val price: Double?,
    val percent: Double?,
    val blink: BlinkDir = BlinkDir.NONE
)


data class HomeUiState(
    val isLoading: Boolean = true,
    val marketStatus: MarketStatusResponse? = null,
    val indices: List<uiIndex> = emptyList(),
    val gainers: List<uiStock> = emptyList(),
    val losers: List<uiStock> = emptyList(),
    val stocks: List<uiStock> = emptyList(),
    val nifty50: List<uiStock> = emptyList(),
    val search: String = "",
    val error: String? = null
)
