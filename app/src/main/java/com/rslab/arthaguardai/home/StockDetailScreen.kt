package com.rslab.arthaguardai.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@Composable
fun StockDetailScreen(
    symbol: String,
    onBack: () -> Unit
) {
    val viewModel: StockDetailViewModel = viewModel(
        factory = StockDetailViewModel.Factory(symbol = symbol)
    )
    val ui by viewModel.uiState.collectAsStateWithLifecycle()

    val blink = rememberInfiniteTransition(label = "detailBlink")
    val blinkAlpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "detailBlinkAlpha"
    )

    val ranges = remember { listOf("1D", "1W", "1M", "3M") }
    val points = remember(ui.history) {
        ui.history
    }

    val trendColor = when {
        (ui.percent ?: 0.0) > 0 -> Color(0xFF18A558)
        (ui.percent ?: 0.0) < 0 -> Color(0xFFD94242)
        else -> Color(0xFF6D7587)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF4F8FF),
                        Color(0xFFE7F0FF),
                        Color(0xFFDCE9FF)
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            StockDetailTopBar(symbol = ui.symbol, onBack = onBack)

            when {
                ui.isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF2A5FFF))
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(
                                        text = ui.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = ui.symbol,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF6D7587)
                                    )
                                    Text(
                                        text = ui.instrumentType.uppercase(),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF4A66A8)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = formatPrice(ui.price),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = trendColor,
                                        modifier = Modifier.alpha(
                                            if (ui.blink == BlinkDir.NONE) 1f else blinkAlpha
                                        )
                                    )
                                    Text(
                                        text = formatPercent(ui.percent),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = trendColor,
                                        modifier = Modifier.alpha(
                                            if (ui.blink == BlinkDir.NONE) 1f else blinkAlpha
                                        )
                                    )
                                }
                            }
                        }

                        item {
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        ranges.forEach { range ->
                                            RangeChip(
                                                label = range,
                                                selected = ui.selectedRange == range,
                                                onClick = { viewModel.onRangeSelected(range) }
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(12.dp))
                                    if (points.isEmpty()) {
                                        Text(
                                            text = "Chart is loading...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = Color(0xFF6D7587)
                                        )
                                    } else {
                                        PriceLineChart(
                                            points = points,
                                            lineColor = trendColor,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(220.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Low ${formatPrice(ui.low)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6D7587)
                                        )
                                        Text(
                                            text = "High ${formatPrice(ui.high)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF6D7587)
                                        )
                                    }
                                }
                            }
                        }

                        if (ui.error != null) {
                            item {
                                Card(
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                                ) {
                                    Text(
                                        text = ui.error ?: "",
                                        color = Color(0xFFD94242),
                                        modifier = Modifier.padding(12.dp),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    label = "Open",
                                    value = formatPrice(ui.open),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Prev Close",
                                    value = formatPrice(ui.previousClose),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                StatCard(
                                    label = "Day High",
                                    value = formatPrice(ui.high),
                                    modifier = Modifier.weight(1f)
                                )
                                StatCard(
                                    label = "Day Low",
                                    value = formatPrice(ui.low),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StockDetailTopBar(symbol: String, onBack: () -> Unit) {
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
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.16f)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }
            Text(
                text = symbol,
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun RangeChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFF2A5FFF) else Color(0xFFEAF0FF),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = if (selected) Color.White else Color(0xFF2A5FFF),
            style = MaterialTheme.typography.labelLarge
        )
    }
}

@Composable
private fun PriceLineChart(
    points: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        if (points.size < 2) return@Canvas

        val minValue = points.minOrNull() ?: return@Canvas
        val maxValue = points.maxOrNull() ?: return@Canvas
        val range = (maxValue - minValue).takeIf { it > 0.0 } ?: 1.0
        val stepX = size.width / (points.size - 1).coerceAtLeast(1)

        val linePath = Path()
        val areaPath = Path()

        points.forEachIndexed { index, value ->
            val x = stepX * index
            val y = size.height - (((value - minValue) / range).toFloat() * size.height)
            if (index == 0) {
                linePath.moveTo(x, y)
                areaPath.moveTo(x, size.height)
                areaPath.lineTo(x, y)
            } else {
                linePath.lineTo(x, y)
                areaPath.lineTo(x, y)
            }
        }

        areaPath.lineTo(size.width, size.height)
        areaPath.close()

        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(
                    lineColor.copy(alpha = 0.24f),
                    lineColor.copy(alpha = 0.02f),
                    Color.Transparent
                )
            )
        )
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )

        val lastX = stepX * (points.size - 1)
        val lastY = size.height - (((points.last() - minValue) / range).toFloat() * size.height)
        drawCircle(color = lineColor, radius = 9f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
        drawCircle(color = Color.White, radius = 4f, center = androidx.compose.ui.geometry.Offset(lastX, lastY))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFFFF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF6D7587)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

private fun formatPrice(value: Double?): String {
    if (value == null) return "--"
    return "\u20B9${String.format(Locale.US, "%,.2f", value)}"
}

private fun formatPercent(value: Double?): String {
    val percent = value ?: 0.0
    val sign = if (percent > 0) "+" else ""
    return "$sign${String.format(Locale.US, "%.2f", percent)}%"
}
