package com.rslab.arthaguardai.stock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StockDetailViewModelFactory(
    private val symbol: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return StockDetailViewModel(symbol) as T
    }
}
