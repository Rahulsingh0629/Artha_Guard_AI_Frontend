package com.rslab.arthaguardai.home

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs

private val PositiveColor = Color(0xFF18A558)
private val NegativeColor = Color(0xFFD94242)
private val NeutralColor = Color(0xFF6D7587)

private sealed interface StockGridCell {
    data class Stock(val value: uiStock) : StockGridCell
    data object ShowMore : StockGridCell
}

@Composable
fun HomeScreen(
    onOpenAdvisory: () -> Unit,
    onOpenStockDetail: (String) -> Unit,
    onOpenAllStocks: () -> Unit,
    onOpenAllMovers: () -> Unit,
    onLogout: () -> Unit,
    vm: HomeViewModel = viewModel()
) {
    val ui by vm.uiState.collectAsStateWithLifecycle()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val blink = rememberInfiniteTransition(label = "homeBlink")
    val blinkAlpha by blink.animateFloat(
        initialValue = 1f,
        targetValue = 0.38f,
        animationSpec = infiniteRepeatable(
            animation = tween(420),
            repeatMode = RepeatMode.Reverse
        ),
        label = "homeBlinkAlpha"
    )

    val filteredStocks = remember(ui.search, ui.nifty50) {
        if (ui.search.isBlank()) {
            ui.nifty50
        } else {
            ui.nifty50.filter {
                it.symbol.contains(ui.search, ignoreCase = true) ||
                    it.name.contains(ui.search, ignoreCase = true)
            }
        }
    }

    val stockBySymbol = remember(ui.stocks) {
        ui.stocks.associateBy { it.symbol.uppercase(Locale.ROOT) }
    }
    val moverPreview = remember(ui.gainers, ui.losers, stockBySymbol) {
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
            .take(5)
    }

    val stockGridCells = remember(filteredStocks) {
        val preview = filteredStocks.take(7).map { StockGridCell.Stock(it) }
        preview + StockGridCell.ShowMore
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerContent(
                onOpenHome = {
                    scope.launch { drawerState.close() }
                },
                onOpenAllMovers = {
                    scope.launch {
                        drawerState.close()
                        onOpenAllMovers()
                    }
                },
                onOpenAllStocks = {
                    scope.launch {
                        drawerState.close()
                        onOpenAllStocks()
                    }
                },
                onOpenAdvisory = {
                    scope.launch {
                        drawerState.close()
                        onOpenAdvisory()
                    }
                },
                onLogout = {
                    scope.launch {
                        drawerState.close()
                        onLogout()
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFF3F9FF),
                            Color(0xFFE5F0FF),
                            Color(0xFFDAE8FF)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                HomeTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onOpenAdvisory = onOpenAdvisory
                )

                when {
                    ui.isLoading -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color(0xFF2A5FFF))
                        }
                    }

                    ui.error != null && ui.stocks.isEmpty() -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = ui.error ?: "Something went wrong",
                                color = NegativeColor,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }

                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            item {
                                OutlinedTextField(
                                    value = ui.search,
                                    onValueChange = vm::onSearchChange,
                                    singleLine = true,
                                    label = { Text("Search any stock") },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Color(0xFF2A5FFF),
                                        unfocusedBorderColor = Color(0xFFADC2E8)
                                    )
                                )
                            }

                            if (ui.error != null) {
                                item {
                                    Card(
                                        shape = RoundedCornerShape(14.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3))
                                    ) {
                                        Text(
                                            text = ui.error ?: "",
                                            color = NegativeColor,
                                            modifier = Modifier.padding(12.dp),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }

                            item {
                                SectionTitle(title = "Indices")
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(ui.indices, key = { it.key }) { idx ->
                                        IndexCard(
                                            index = idx,
                                            blinkAlpha = blinkAlpha,
                                            onClick = { onOpenStockDetail(idx.key) }
                                        )
                                    }
                                }
                            }

                            item {
                                SectionTitle(title = "Top Movers")
                                Spacer(modifier = Modifier.height(8.dp))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(moverPreview, key = { it.symbol }) { mover ->
                                        TopMoverCard(
                                            stock = mover,
                                            blinkAlpha = blinkAlpha,
                                            onClick = { onOpenStockDetail(mover.symbol) }
                                        )
                                    }
                                    item {
                                        ShowMoreCard(
                                            title = "Show more",
                                            subtitle = "All movers",
                                            onClick = onOpenAllMovers
                                        )
                                    }
                                }
                            }

                            item {
                                SectionTitle(title = "NIFTY 50")
                            }

                            items(stockGridCells.chunked(2)) { row ->
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    row.forEach { cell ->
                                        when (cell) {
                                            is StockGridCell.Stock -> StockCard(
                                                stock = cell.value,
                                                blinkAlpha = blinkAlpha,
                                                modifier = Modifier.weight(1f),
                                                onClick = { onOpenStockDetail(cell.value.symbol) }
                                            )

                                            StockGridCell.ShowMore -> ShowMoreCard(
                                                title = "Show more",
                                                subtitle = "All stocks",
                                                modifier = Modifier.weight(1f),
                                                onClick = onOpenAllStocks
                                            )
                                        }
                                    }
                                    if (row.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeDrawerContent(
    onOpenHome: () -> Unit,
    onOpenAllMovers: () -> Unit,
    onOpenAllStocks: () -> Unit,
    onOpenAdvisory: () -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = Color(0xFFF7FAFF)
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "ArthaGuard AI",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF102446),
            modifier = Modifier.padding(horizontal = 18.dp)
        )
        Text(
            text = "Market dashboard",
            style = MaterialTheme.typography.bodySmall,
            color = NeutralColor,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)
        )
        Spacer(modifier = Modifier.height(10.dp))

        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = onOpenHome,
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        NavigationDrawerItem(
            label = { Text("Top Movers") },
            selected = false,
            onClick = onOpenAllMovers,
            icon = { Icon(Icons.Default.ShowChart, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        NavigationDrawerItem(
            label = { Text("All Stocks") },
            selected = false,
            onClick = onOpenAllStocks,
            icon = { Icon(Icons.Default.Menu, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        NavigationDrawerItem(
            label = { Text("AI Advisory") },
            selected = false,
            onClick = onOpenAdvisory,
            icon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
        Spacer(modifier = Modifier.height(6.dp))
        HorizontalDivider(
            color = Color(0xFFDCE4F6),
            modifier = Modifier.padding(horizontal = 14.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(unselectedContainerColor = Color.Transparent)
        )
    }
}

@Composable
private fun HomeTopBar(
    onMenuClick: () -> Unit,
    onOpenAdvisory: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF0E1C38),
        shadowElevation = 12.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 30.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onMenuClick) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Text(
                text = "ArthaGuard AI",
                color = Color.White,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenAdvisory) {
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "Advisor",
                            tint = Color.White,
                            modifier = Modifier.padding(6.dp)
                        )
                    }
                }
                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile",
                        tint = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = Color(0xFF102446)
    )
}

@Composable
private fun IndexCard(
    index: uiIndex,
    blinkAlpha: Float,
    onClick: () -> Unit
) {
    val color = percentColor(index.percent)
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF6F8FD)),
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = index.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatPrice(index.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = formatPercent(index.percent),
                color = color,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.alpha(if (index.blink == BlinkDir.NONE) 1f else blinkAlpha)
            )
        }
    }
}

@Composable
private fun TopMoverCard(
    stock: uiStock,
    blinkAlpha: Float,
    onClick: () -> Unit
) {
    val color = percentColor(stock.percent)
    Card(
        modifier = Modifier
            .width(168.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFDFDFE))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            HorizontalDivider(color = Color(0xFFE1E6F2), thickness = 1.dp)
            Text(
                text = formatPrice(stock.price),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.alpha(if (stock.blink == BlinkDir.NONE) 1f else blinkAlpha)
            )
            Text(
                text = formatPercent(stock.percent),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier.alpha(if (stock.blink == BlinkDir.NONE) 1f else blinkAlpha)
            )
        }
    }
}

@Composable
private fun StockCard(
    stock: uiStock,
    blinkAlpha: Float,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val color = percentColor(stock.percent)
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = stock.symbol,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stock.name,
                style = MaterialTheme.typography.bodySmall,
                color = NeutralColor,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formatPrice(stock.price),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.alpha(if (stock.blink == BlinkDir.NONE) 1f else blinkAlpha)
            )
            Text(
                text = formatPercent(stock.percent),
                style = MaterialTheme.typography.bodyMedium,
                color = color,
                modifier = Modifier.alpha(if (stock.blink == BlinkDir.NONE) 1f else blinkAlpha)
            )
        }
    }
}

@Composable
private fun ShowMoreCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier.width(168.dp),
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .height(136.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF4FF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF375A9E)
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF375A9E),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun percentColor(percent: Double?): Color {
    val value = percent ?: 0.0
    return when {
        value > 0 -> PositiveColor
        value < 0 -> NegativeColor
        else -> NeutralColor
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
