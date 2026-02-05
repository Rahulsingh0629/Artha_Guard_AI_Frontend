package com.rslab.arthaguardai.dashboard

import com.rslab.arthaguardai.network.*

data class DashboardUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,

    val marketStatus: MarketStatusResponse? = null,

    val nifty: IndexItem? = null,
    val sensex: IndexItem? = null,
    val bankNifty: IndexItem? = null,

    val topGainers: List<TopMoverItem> = emptyList(),
    val topLosers: List<TopMoverItem> = emptyList(),

    val stocks: List<StockItem> = emptyList(),

    val error: String? = null
)
