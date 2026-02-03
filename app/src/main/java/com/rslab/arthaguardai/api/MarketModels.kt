package com.rslab.arthaguardai.api

import com.google.gson.annotations.SerializedName

data class MarketIndicesResponse(
    @SerializedName("nifty_50")
    val nifty50: IndexData,

    @SerializedName("sensex")
    val sensex: IndexData
)

data class IndexData(
    val available: Boolean,
    val price: String,
    val change: String,

    @SerializedName("change_percent")
    val changePercent: String,

    val positive: Boolean
)
