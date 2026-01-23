package com.rslab.arthaguardai.dashboard

data class DashboardUiState(
    val loading: Boolean = true,

    val niftyValue: String = "--",
    val niftyChange: String = "--",
    val niftyPositive: Boolean = true,

    val sensexValue: String = "--",
    val sensexChange: String = "--",
    val sensexPositive: Boolean = true,

    val error: String? = null
)
