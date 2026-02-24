package com.rslab.arthaguardai.scanner

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.ui.components.HomeBackgroundBrush
import com.rslab.arthaguardai.ui.components.HomeStyleTopBar
import com.rslab.arthaguardai.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IntradayScannerScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var items by remember { mutableStateOf<List<ScannerStock>>(emptyList()) }
    var page by remember { mutableStateOf(1) }
    var totalPages by remember { mutableStateOf(1) }
    var totalStocks by remember { mutableStateOf(0) }
    var watchlistCount by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadPage(targetPage: Int) {
        scope.launch {
            isLoading = true
            error = null
            try {
                val token = sessionManager.fetchAuthToken()
                if (token.isNullOrBlank()) {
                    error = "Session expired. Please login again."
                    return@launch
                }
                val authHeader = if (token.trim().startsWith("Bearer", ignoreCase = true)) {
                    token
                } else {
                    "Bearer $token"
                }

                val response = RetrofitInstance.api.getScannerMarket(
                    token = authHeader,
                    page = targetPage,
                    pageSize = 20
                )

                items = parseScannerItems(response.array("items"))
                page = response.intValue("page") ?: targetPage
                totalPages = (response.intValue("total_pages") ?: 1).coerceAtLeast(1)
                totalStocks = response.intValue("total") ?: 0
                watchlistCount = response.intValue("watchlist_count") ?: 0
            } catch (e: Exception) {
                error = e.message ?: "Failed to load scanner data."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadPage(targetPage = 1)
    }

    Scaffold(
        topBar = {
            HomeStyleTopBar(
                title = "ArthaGuard AI",
                subtitle = "Scanner",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Back",
                onNavigationClick = onBack,
                primaryActionIcon = Icons.Default.Refresh,
                primaryActionContentDescription = "Refresh scan",
                onPrimaryActionClick = { if (!isLoading) loadPage(1) }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HomeBackgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Text(
                    text = "Intraday Scanner",
                    color = Color(0xFF102446),
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { loadPage(1) }, enabled = !isLoading) {
                        Text("Refresh Scan")
                    }
                    Text(
                        text = "Page $page / $totalPages",
                        color = Color(0xFF64748B),
                        modifier = Modifier.align(Alignment.CenterVertically)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (!error.isNullOrBlank()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = error.orEmpty(),
                            color = Color(0xFFD94242),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Market Scan ($totalStocks stocks) | Watchlist: $watchlistCount",
                            color = Color(0xFF102446),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = Color(0xFF2A5FFF))
                            }
                        } else {
                            ScannerTable(items = items)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))

                PaginationBar(
                    currentPage = page,
                    totalPages = totalPages,
                    onPrev = { if (page > 1) loadPage(page - 1) },
                    onNext = { if (page < totalPages) loadPage(page + 1) },
                    onPageClick = { p -> if (p != page) loadPage(p) }
                )
            }
        }
    }
}

@Composable
private fun ScannerTable(items: List<ScannerStock>) {
    val horizontal = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(horizontal)
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFFE8F0FF))
                .padding(vertical = 8.dp, horizontal = 6.dp)
        ) {
            ScannerHeader("Stock", 110.dp)
            ScannerHeader("Price", 88.dp)
            ScannerHeader("Trend", 92.dp)
            ScannerHeader("RSI", 70.dp)
            ScannerHeader("Volume %", 92.dp)
            ScannerHeader("Score", 74.dp)
            ScannerHeader("Signal", 140.dp)
        }

        items.forEach { stock ->
            Row(
                modifier = Modifier
                    .padding(vertical = 6.dp, horizontal = 6.dp)
                    .background(Color(0xFFF8FAFF))
            ) {
                ScannerCell(stock.symbol, 110.dp, fontWeight = FontWeight.SemiBold)
                ScannerCell(stock.currentPrice?.toPrice() ?: "-", 88.dp)
                ScannerCell(stock.trend ?: "-", 92.dp, color = trendColor(stock.trend))
                ScannerCell(stock.rsi?.toCompact() ?: "-", 70.dp)
                ScannerCell(stock.volumeChangePct?.toCompact() ?: "-", 92.dp)
                ScannerCell(stock.score?.toCompact() ?: "-", 74.dp)
                ScannerCell(stock.signalText(), 140.dp, color = signalColor(stock.signalText()))
            }
        }
    }
}

@Composable
private fun ScannerHeader(text: String, width: androidx.compose.ui.unit.Dp) {
    Text(
        text = text,
        color = Color(0xFF102446),
        fontWeight = FontWeight.Bold,
        modifier = Modifier.width(width)
    )
}

@Composable
private fun ScannerCell(
    text: String,
    width: androidx.compose.ui.unit.Dp,
    color: Color = Color(0xFF0F172A),
    fontWeight: FontWeight = FontWeight.Normal
) {
    Text(
        text = text,
        color = color,
        modifier = Modifier.width(width),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontSize = 14.sp,
        fontWeight = fontWeight
    )
}

@Composable
private fun PaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onPageClick: (Int) -> Unit
) {
    val tokens = buildPageTokens(currentPage = currentPage, totalPages = totalPages)

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            modifier = Modifier.width(56.dp),
            onClick = onPrev,
            enabled = currentPage > 1
        ) {Icon(Icons.Default.ChevronLeft, contentDescription = "Previous") }

        Box(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                tokens.forEach { token ->
                    when (token) {
                        is PageToken.Page -> {
                            val selected = token.value == currentPage
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) Color(0xFF2A5FFF) else Color.Transparent
                            ) {
                                TextButton(onClick = { onPageClick(token.value) }) {
                                    Text(
                                        text = token.value.toString(),
                                        color = if (selected) Color.White else Color(0xFF1D4ED8)
                                    )
                                }
                            }
                        }

                        PageToken.Ellipsis -> {
                            Text("...", color = Color(0xFF1D4ED8))
                        }
                    }
                }
            }
        }

        OutlinedButton(
            modifier = Modifier.width(56.dp),
            onClick = onNext,
            enabled = currentPage < totalPages
        ) { Icon(Icons.Default.ChevronRight, contentDescription = "Next") }
    }
}

private data class ScannerStock(
    val symbol: String,
    val currentPrice: Double?,
    val trend: String?,
    val rsi: Double?,
    val volumeChangePct: Double?,
    val score: Double?,
    val error: String?
)

private sealed interface PageToken {
    data class Page(val value: Int) : PageToken
    data object Ellipsis : PageToken
}

private fun buildPageTokens(currentPage: Int, totalPages: Int): List<PageToken> {
    val tokens = mutableListOf<PageToken>()

    if (totalPages <= 5) {
        for(i in 1..totalPages){
            tokens.add(PageToken.Page(i))
        }
        return tokens
    }

    val start = maxOf(2, currentPage - 1)
    val end = minOf(totalPages - 1, currentPage + 1)

    tokens.add(PageToken.Page(1))

    if (start > 2) tokens.add(PageToken.Ellipsis)

    for (i in start..end) {
        tokens.add(PageToken.Page(i))
    }

    if (end < totalPages - 1) tokens.add(PageToken.Ellipsis)

    tokens.add(PageToken.Page(totalPages))
    return tokens
}

private fun ScannerStock.signalText(): String {
    if (!error.isNullOrBlank()) return "Data Unavailable"
    val normalizedTrend = trend.orEmpty().uppercase(Locale.US)
    val rsiValue = rsi ?: 0.0
    return when {
        normalizedTrend.contains("BULL") && rsiValue >= 70 -> "Overbought Risk"
        normalizedTrend.contains("BULL") -> "Momentum Positive"
        normalizedTrend.contains("BEAR") && rsiValue <= 35 -> "Oversold Watch"
        normalizedTrend.contains("BEAR") -> "Downtrend"
        else -> "Neutral"
    }
}

private fun trendColor(trend: String?): Color {
    val value = trend.orEmpty().uppercase(Locale.US)
    return when {
        value.contains("BULL") -> Color(0xFF22C55E)
        value.contains("BEAR") -> Color(0xFFEF4444)
        else -> Color(0xFFE2E8F0)
    }
}

private fun signalColor(signal: String): Color {
    return when {
        signal.contains("Positive", ignoreCase = true) -> Color(0xFF22C55E)
        signal.contains("Risk", ignoreCase = true) || signal.contains("Downtrend", ignoreCase = true) -> Color(0xFFEF4444)
        signal.contains("Watch", ignoreCase = true) -> Color(0xFFF59E0B)
        else -> Color(0xFFE2E8F0)
    }
}

private fun Double.toCompact(): String = String.format(Locale.US, "%.2f", this)
private fun Double.toPrice(): String = String.format(Locale.US, "%.2f", this)

private fun parseScannerItems(array: JsonArray?): List<ScannerStock> {
    if (array == null) return emptyList()
    return buildList {
        array.forEach { element ->
            val obj = element.asJsonObjectOrNull() ?: return@forEach
            add(
                ScannerStock(
                    symbol = obj.stringValue("symbol").orEmpty().ifBlank { "--" },
                    currentPrice = obj.doubleValue("current_price"),
                    trend = obj.stringValue("trend"),
                    rsi = obj.doubleValue("rsi"),
                    volumeChangePct = obj.doubleValue("volume_change_pct"),
                    score = obj.doubleValue("volatility_score"),
                    error = obj.stringValue("error")
                )
            )
        }
    }
}

private fun JsonObject.array(key: String): JsonArray? {
    val value = get(key) ?: return null
    return if (value.isJsonArray) value.asJsonArray else null
}

private fun JsonObject.intValue(key: String): Int? {
    val value = get(key) ?: return null
    return value.intOrNull()
}

private fun JsonObject.doubleValue(key: String): Double? {
    val value = get(key) ?: return null
    return value.doubleOrNull()
}

private fun JsonObject.stringValue(key: String): String? {
    val value = get(key) ?: return null
    return value.stringOrNull()
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
    return if (isJsonObject) asJsonObject else null
}

private fun JsonElement.stringOrNull(): String? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asString else toString()
    }.getOrNull()
}

private fun JsonElement.intOrNull(): Int? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asInt else null
    }.getOrNull()
}

private fun JsonElement.doubleOrNull(): Double? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asDouble else null
    }.getOrNull()
}
