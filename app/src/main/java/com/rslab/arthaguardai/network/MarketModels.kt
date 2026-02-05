package com.rslab.arthaguardai.network

// -------------------- MARKET STATUS --------------------

data class MarketStatusResponse(
    val status: String,
    val reason: String? = null
)

// -------------------- INDICES --------------------

data class IndicesResponse(
    val nifty: IndexItem,
    val sensex: IndexItem,
    val bankNifty: IndexItem? = null,
    val finNifty: IndexItem? = null
)

data class IndexItem(
    val name: String,
    val price: Double,
    val change: Double,
    val percent: Double
)

// -------------------- STOCK LIST --------------------

data class StocksResponse(
    val count: Int,
    val stocks: List<StockItem>
)

data class StockItem(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val percent: Double
)

// -------------------- TOP MOVERS --------------------

data class TopMoversResponse(
    val gainers: List<TopMoverItem>,
    val losers: List<TopMoverItem>
)

data class TopMoverItem(
    val symbol: String,
    val percent: Double
)

// -------------------- STOCK DETAIL --------------------

data class StockDetailResponse(
    val symbol: String,
    val name: String,
    val price: Double,
    val change: Double,
    val percent: Double,
    val open: Double,
    val high: Double,
    val low: Double,
    val previousClose: Double
)
