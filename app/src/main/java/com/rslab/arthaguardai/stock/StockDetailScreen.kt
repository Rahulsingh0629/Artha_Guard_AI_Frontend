package com.rslab.arthaguardai.stock

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StockDetailScreen(
    symbol: String
) {
    val viewModel: StockDetailViewModel = viewModel(
        factory = StockDetailViewModelFactory(symbol)
    )
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = Color(0xFF0B0F14)
    ) { padding ->

        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.error ?: "Error", color = Color.White)
                }
            }

            else -> {
                StockDetailContent(state, Modifier.padding(padding))
            }
        }
    }
}

@Composable
fun StockDetailContent(
    state: StockDetailUiState,
    modifier: Modifier
) {
    val isPositive = state.change >= 0
    val changeColor by animateColorAsState(
        if (isPositive) Color(0xFF22C55E) else Color(0xFFEF4444)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(state.name, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(state.symbol, color = Color.Gray)

        Spacer(Modifier.height(12.dp))

        Text(
            text = "₹${state.price}",
            color = Color.White,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "${state.change} (${state.percent}%)",
            color = changeColor,
            fontSize = 16.sp
        )

        Spacer(Modifier.height(20.dp))

        // 🔥 CHART PLACEHOLDER
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(Color(0xFF111827), RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("Price Chart (Next Step)", color = Color.Gray)
        }

        Spacer(Modifier.height(20.dp))

        // 🔹 OHLC
        OHLCRow("Open", state.open)
        OHLCRow("High", state.high)
        OHLCRow("Low", state.low)
        OHLCRow("Prev Close", state.previousClose)
    }
}


@Composable
fun OHLCRow(label: String, value: Double) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray)
        Text("₹$value", color = Color.White, fontWeight = FontWeight.Medium)
    }
}

