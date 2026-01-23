package com.rslab.arthaguardai.api

import com.google.gson.annotations.SerializedName

data class IndicesResponse(
    val nifty: GlobalQuoteWrapper,
    val sensex: GlobalQuoteWrapper
)
data class GlobalQuoteWrapper(
    @SerializedName("Global Quote")
    val quote: GlobalQuote
)
data class GlobalQuote(
    @SerializedName("05. price")
    val price: String,

    @SerializedName("09. change")
    val change: String,

    @SerializedName("10. change percent")
    val changePercent: String
)
