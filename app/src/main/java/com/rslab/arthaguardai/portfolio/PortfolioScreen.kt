package com.rslab.arthaguardai.portfolio

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.rslab.arthaguardai.network.PortfolioAddRequest
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.ui.components.HomeBackgroundBrush
import com.rslab.arthaguardai.ui.components.HomeStyleTopBar
import com.rslab.arthaguardai.utils.SessionManager
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var actionInfo by remember { mutableStateOf<String?>(null) }
    var analysis by remember { mutableStateOf<PortfolioAnalysisUi?>(null) }
    var showAddForm by remember { mutableStateOf(false) }

    var symbolInput by remember { mutableStateOf("") }
    var quantityInput by remember { mutableStateOf("1") }
    var buyPriceInput by remember { mutableStateOf("") }
    var sectorInput by remember { mutableStateOf("Unknown") }
    var buyDateTimeInput by remember { mutableStateOf("") }

    fun resolveAuthHeader(): String? {
        val token = sessionManager.fetchAuthToken()
        if (token.isNullOrBlank()) {
            error = "Session expired. Please login again."
            return null
        }
        return if (token.trim().startsWith("Bearer", ignoreCase = true)) token else "Bearer $token"
    }

    suspend fun fetchPortfolioWithAuth(authHeader: String) {
        val response = RetrofitInstance.api.getPortfolioAnalysis(authHeader)
        analysis = parsePortfolioAnalysis(response)
    }

    fun fetchPortfolio() {
        scope.launch {
            isLoading = true
            error = null
            try {
                val authHeader = resolveAuthHeader() ?: return@launch
                fetchPortfolioWithAuth(authHeader)
            } catch (e: Exception) {
                error = e.message ?: "Failed to load portfolio analysis."
            } finally {
                isLoading = false
            }
        }
    }

    fun addSingleStockAndAnalyze() {
        val symbol = symbolInput.trim().uppercase(Locale.US)
        val quantity = quantityInput.trim().toIntOrNull()
        val buyPrice = buyPriceInput.trim().toDoubleOrNull()
        val sector = sectorInput.trim().ifBlank { "Unknown" }
        val buyDateTime = buyDateTimeInput.trim().ifBlank { null }

        when {
            symbol.isBlank() -> {
                error = "Stock symbol is required."
                return
            }
            quantity == null || quantity <= 0 -> {
                error = "Quantity must be a positive whole number."
                return
            }
            buyPrice == null || buyPrice <= 0.0 -> {
                error = "Buy price must be a positive number."
                return
            }
        }

        scope.launch {
            isLoading = true
            error = null
            actionInfo = null
            try {
                val authHeader = resolveAuthHeader() ?: return@launch
                val response = RetrofitInstance.api.addPortfolioStock(
                    token = authHeader,
                    request = PortfolioAddRequest(
                        symbol = symbol,
                        quantity = quantity,
                        buy_price = buyPrice,
                        sector = sector,
                        buy_datetime = buyDateTime
                    )
                )
                val warnings = response.array("warnings")?.size() ?: 0
                actionInfo = if (warnings > 0) {
                    "Added $symbol with $warnings warning(s)."
                } else {
                    "Added $symbol successfully."
                }

                symbolInput = ""
                quantityInput = "1"
                buyPriceInput = ""
                buyDateTimeInput = ""
                showAddForm = false

                fetchPortfolioWithAuth(authHeader)
            } catch (e: Exception) {
                error = e.message ?: "Failed to add stock."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchPortfolio()
    }

    Scaffold(
        topBar = {
            HomeStyleTopBar(
                title = "Portfolio",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Back",
                onNavigationClick = onBack,
                primaryActionIcon = if (showAddForm) Icons.Default.Close else Icons.Default.Add,
                primaryActionContentDescription = if (showAddForm) "Close add form" else "Add stock",
                onPrimaryActionClick = { showAddForm = !showAddForm }
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
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(onClick = { fetchPortfolio() }) {
                            Text("Refresh")
                        }
                    }
                }

                if (showAddForm) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = "Add Stock For Analysis",
                                    color = Color(0xFF102446),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = symbolInput,
                                    onValueChange = { symbolInput = it },
                                    label = { Text("Symbol (e.g. RELIANCE)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = quantityInput,
                                    onValueChange = { quantityInput = it },
                                    label = { Text("Quantity") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = buyPriceInput,
                                    onValueChange = { buyPriceInput = it },
                                    label = { Text("Buy Price") },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = sectorInput,
                                    onValueChange = { sectorInput = it },
                                    label = { Text("Sector (optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = buyDateTimeInput,
                                    onValueChange = { buyDateTimeInput = it },
                                    label = { Text("Buy Datetime ISO (optional)") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = { addSingleStockAndAnalyze() },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Analyze")
                                    }
                                    Button(
                                        onClick = { showAddForm = false },
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text("Cancel")
                                    }
                                }
                            }
                        }
                    }
                }

                if (!error.isNullOrBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = error.orEmpty(),
                                color = Color(0xFFD94242),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                if (!actionInfo.isNullOrBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEEFCE9)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = actionInfo.orEmpty(),
                                color = Color(0xFF18A558),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }

                val analysisSnapshot = analysis
                if (analysisSnapshot == null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = "No portfolio data yet. Add a stock to get analysis cards.",
                                color = Color(0xFF64748B),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }
                } else {
                    item {
                        SummaryCard(summary = analysisSnapshot.summary)
                    }
                    item {
                        RiskCard(risk = analysisSnapshot.risk)
                    }
                    item {
                        Text(
                            text = "Holdings (${analysisSnapshot.holdings.size})",
                            color = Color(0xFF102446),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    items(analysisSnapshot.holdings) { holding ->
                        HoldingCard(holding = holding)
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color(0xFF2DD4BF))
                }
            }
        }
    }
}

@Composable
private fun SummaryCard(summary: PortfolioSummaryUi) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Portfolio Summary",
                color = Color(0xFF102446),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LabelValueRow("Invested", "Rs ${summary.totalInvestment.toMoney()}")
            LabelValueRow("Current Value", "Rs ${summary.currentValue.toMoney()}")
            LabelValueRow(
                "Total P&L",
                "Rs ${summary.totalPnl.toMoney()} (${summary.totalPnlPercent.toPercent()})",
                valueColor = pnlColor(summary.totalPnl)
            )
            if (summary.asOf.isNotBlank()) {
                LabelValueRow("As Of", summary.asOf, valueColor = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun RiskCard(risk: PortfolioRiskUi) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Risk Profile",
                color = Color(0xFF102446),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            LabelValueRow("Score", risk.score.toMoney())
            LabelValueRow("Level", risk.level, valueColor = riskLevelColor(risk.level))
            if (risk.alerts.isNotEmpty()) {
                Text(
                    text = "Alerts",
                    color = Color(0xFFE53B3B),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                risk.alerts.take(4).forEach { alert ->
                    Text(
                        text = "- $alert",
                        color = Color(0xFFE57373),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun HoldingCard(holding: HoldingUi) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = holding.symbol,
                color = Color(0xFF102446),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            LabelValueRow("Sector", holding.sector)
            LabelValueRow("Quantity", holding.qty.toString())
            LabelValueRow("Buy Price", "Rs ${holding.buyPrice.toMoney()}")
            LabelValueRow("Current Price", "Rs ${holding.currentPrice.toMoney()}")
            LabelValueRow("Invested", "Rs ${holding.investedValue.toMoney()}")
            LabelValueRow("Current Value", "Rs ${holding.currentValue.toMoney()}")
            LabelValueRow(
                "P&L",
                "Rs ${holding.pnl.toMoney()} (${holding.pnlPercent.toPercent()})",
                valueColor = pnlColor(holding.pnl)
            )
            LabelValueRow("Holding Days", holding.holdingDays.toString())
            if (holding.taxType.isNotBlank()) {
                LabelValueRow("Tax Type", holding.taxType)
            }
            if (holding.estimatedTax > 0.0) {
                LabelValueRow("Estimated Tax", "Rs ${holding.estimatedTax.toMoney()}")
            }
            if (holding.priceSource.isNotBlank()) {
                LabelValueRow("Price Source", holding.priceSource, valueColor = Color(0xFF64748B))
            }
        }
    }
}

@Composable
private fun LabelValueRow(
    label: String,
    value: String,
    valueColor: Color = Color(0xFF0F172A)
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0xFF64748B),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class PortfolioAnalysisUi(
    val summary: PortfolioSummaryUi,
    val risk: PortfolioRiskUi,
    val holdings: List<HoldingUi>
)

private data class PortfolioSummaryUi(
    val totalInvestment: Double,
    val currentValue: Double,
    val totalPnl: Double,
    val totalPnlPercent: Double,
    val asOf: String
)

private data class PortfolioRiskUi(
    val score: Double,
    val level: String,
    val alerts: List<String>
)

private data class HoldingUi(
    val symbol: String,
    val sector: String,
    val qty: Int,
    val buyPrice: Double,
    val currentPrice: Double,
    val investedValue: Double,
    val currentValue: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val holdingDays: Int,
    val taxType: String,
    val estimatedTax: Double,
    val priceSource: String
)

private fun parsePortfolioAnalysis(root: JsonObject): PortfolioAnalysisUi {
    val summaryObj = root.obj("summary")
    val riskObj = root.obj("risk_profile")
    val holdingsArr = root.array("holdings")

    val summary = PortfolioSummaryUi(
        totalInvestment = summaryObj?.doubleValue("total_investment") ?: 0.0,
        currentValue = summaryObj?.doubleValue("current_value") ?: 0.0,
        totalPnl = summaryObj?.doubleValue("total_pnl") ?: 0.0,
        totalPnlPercent = summaryObj?.doubleValue("total_pnl_percent") ?: 0.0,
        asOf = summaryObj?.stringValue("as_of").orEmpty()
    )

    val alerts = riskObj?.array("alerts")
        ?.mapNotNull { it.stringOrNull() }
        ?: emptyList()

    val risk = PortfolioRiskUi(
        score = riskObj?.doubleValue("score") ?: 0.0,
        level = riskObj?.stringValue("level").orEmpty().ifBlank { "UNKNOWN" },
        alerts = alerts
    )

    val holdings = holdingsArr
        ?.mapNotNull { it.asJsonObjectOrNull() }
        ?.map { item ->
            val tax = item.obj("tax_analysis")
            HoldingUi(
                symbol = item.stringValue("symbol").orEmpty().ifBlank { "--" },
                sector = item.stringValue("sector").orEmpty().ifBlank { "Unknown" },
                qty = item.intValue("qty") ?: 0,
                buyPrice = item.doubleValue("buy_price") ?: 0.0,
                currentPrice = item.doubleValue("current_price") ?: 0.0,
                investedValue = item.doubleValue("invested_value") ?: 0.0,
                currentValue = item.doubleValue("current_value") ?: 0.0,
                pnl = item.doubleValue("pnl") ?: 0.0,
                pnlPercent = item.doubleValue("pnl_percent") ?: 0.0,
                holdingDays = item.intValue("holding_days") ?: 0,
                taxType = tax?.stringValue("type").orEmpty(),
                estimatedTax = tax?.doubleValue("estimated_tax") ?: 0.0,
                priceSource = item.stringValue("price_source").orEmpty()
            )
        }
        ?: emptyList()

    return PortfolioAnalysisUi(
        summary = summary,
        risk = risk,
        holdings = holdings
    )
}

private fun JsonObject.obj(key: String): JsonObject? {
    val value = get(key) ?: return null
    return if (value.isJsonObject) value.asJsonObject else null
}

private fun JsonObject.array(key: String): JsonArray? {
    val value = get(key) ?: return null
    return if (value.isJsonArray) value.asJsonArray else null
}

private fun JsonObject.stringValue(key: String): String? {
    val value = get(key) ?: return null
    return value.stringOrNull()
}

private fun JsonObject.intValue(key: String): Int? {
    val value = get(key) ?: return null
    return value.intOrNull()
}

private fun JsonObject.doubleValue(key: String): Double? {
    val value = get(key) ?: return null
    return value.doubleOrNull()
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

private fun Double.toMoney(): String = String.format(Locale.US, "%.2f", this)
private fun Double.toPercent(): String = String.format(Locale.US, "%.2f%%", this)

private fun pnlColor(pnl: Double): Color {
    return if (pnl >= 0.0) Color(0xFF22C55E) else Color(0xFFEF4444)
}

private fun riskLevelColor(level: String): Color {
    return when (level.uppercase(Locale.US)) {
        "LOW" -> Color(0xFF22C55E)
        "MEDIUM" -> Color(0xFFF59E0B)
        "HIGH", "SCAM" -> Color(0xFFEF4444)
        else -> Color(0xFF0F172A)
    }
}
