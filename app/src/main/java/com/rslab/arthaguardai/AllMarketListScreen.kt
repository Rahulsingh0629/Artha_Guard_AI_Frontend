package com.rslab.arthaguardai.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlin.math.abs

@Composable
fun AllMarketListScreen(
    mode: String,
    onBack: () -> Unit,
    onStockClick: (String) -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()

    val title = when (mode) {
        "movers", "gainers", "losers" -> "Top Movers"
        else -> "NIFTY 50 Stocks"
    }

    val stockBySymbol = remember(ui.stocks) {
        ui.stocks.associateBy { it.symbol.uppercase(Locale.ROOT) }
    }

    val movers = remember(ui.gainers, ui.losers, stockBySymbol) {
        (ui.gainers + ui.losers)
            .distinctBy { it.symbol.uppercase(Locale.ROOT) }
            .map { mover ->
                val stock = stockBySymbol[mover.symbol.uppercase(Locale.ROOT)]
                mover.copy(
                    name = stock?.name ?: mover.name,
                    price = stock?.price ?: mover.price,
                    blink = stock?.blink ?: mover.blink
                )
            }
            .sortedByDescending { abs(it.percent ?: 0.0) }
    }

    val data = when (mode) {
        "movers", "gainers", "losers" -> movers
        else -> ui.nifty50
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F8FF),
                        Color(0xFFE9F2FF)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            AllStockTopBar(title = title, onBack = onBack)

            when {
                ui.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2A5FFF))
                    }
                }

                ui.error != null && data.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = ui.error ?: "Failed to load data",
                            color = Color(0xFFD94242)
                        )
                    }
                }

                data.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "No stocks available.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(data, key = { it.symbol }) { stock ->
                            AllStockRow(
                                stock = stock,
                                onClick = { onStockClick(stock.symbol) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AllStockTopBar(title: String, onBack: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0E1C38),
        shadowElevation = 10.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun AllStockRow(
    stock: uiStock,
    onClick: () -> Unit
) {
    val color = when {
        (stock.percent ?: 0.0) > 0 -> Color(0xFF18A558)
        (stock.percent ?: 0.0) < 0 -> Color(0xFFD94242)
        else -> Color(0xFF6D7587)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stock.name,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF6D7587),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (stock.price == null) "--" else "\u20B9${String.format(Locale.US, "%,.2f", stock.price)}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                val percent = stock.percent ?: 0.0
                val sign = if (percent > 0) "+" else ""
                Text(
                    text = "$sign${String.format(Locale.US, "%.2f", percent)}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = color
                )
            }
        }
    }
}
