package com.rslab.arthaguardai.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.rslab.arthaguardai.network.*
import kotlinx.coroutines.launch

/* =========================================================
   MAIN DASHBOARD SCREEN
   ========================================================= */

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DashboardScreen(
    userName: String,
    navController: NavHostController,
    viewModel: DashboardViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isRefreshing,
        onRefresh = { viewModel.refresh() }
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            DashboardDrawer {
                scope.launch { drawerState.close() }
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF0B0F14), // 🔥 Groww dark background
            topBar = {
                DashboardTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onSearchClick = { /* later */ },
                    onProfileClick = { /* later */ }
                )
            },
            bottomBar = { BottomNavigationBar() }
        ) { padding ->

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .pullRefresh(pullRefreshState)
            ) {

                when {
                    state.isLoading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    state.error != null -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(state.error ?: "Something went wrong", color = Color.White)
                        }
                    }

                    else -> {
                        DashboardContent(state, navController)
                    }
                }

                PullRefreshIndicator(
                    refreshing = state.isRefreshing,
                    state = pullRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }
}

/* =========================================================
   DASHBOARD CONTENT (Groww Style)
   ========================================================= */

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    navController: NavHostController
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F14))
            .padding(horizontal = 12.dp)
    ) {

        // 🔹 Market Status
        item {
            MarketStatusBadge(state.marketStatus)
            Spacer(Modifier.height(12.dp))
        }

        // 🔹 Indices
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                state.nifty?.let { IndexCard(it) }
                state.sensex?.let { IndexCard(it) }
            }
        }

        item {
            Spacer(Modifier.height(12.dp))
            state.bankNifty?.let {
                Row { IndexCard(it) }
            }
        }

        // 🔹 All Stocks
        item {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "All Stocks",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
        }

        items(state.stocks) { stock ->
            GrowwStockRow(stock) {
                navController.navigate("stock/${stock.symbol}")
            }
        }

        item { Spacer(Modifier.height(80.dp)) }
    }
}

/* =========================================================
   TOP BAR (UNCHANGED)
   ========================================================= */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    TopAppBar(
        title = { Text("ArthaGuard", fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, null)
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(Icons.Default.Search, null)
            }
            IconButton(onClick = onProfileClick) {
                Icon(Icons.Default.AccountCircle, null)
            }
        }
    )
}

/* =========================================================
   DRAWER (UNCHANGED)
   ========================================================= */

@Composable
fun DashboardDrawer(onItemClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .padding(16.dp)
    ) {
        Text("Menu", fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        listOf(
            "Dashboard", "Profile", "Portfolio",
            "Scanner", "Advisory AI", "News",
            "Fraud Check", "Settings", "Logout"
        ).forEach {
            Text(
                text = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick() }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

/* =========================================================
   BOTTOM NAV (UNCHANGED)
   ========================================================= */

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(true, {}, { Icon(Icons.Default.ShowChart, null) }, label = { Text("Stocks") })
        NavigationBarItem(false, {}, { Icon(Icons.Default.TrendingUp, null) }, label = { Text("F&O") })
        NavigationBarItem(false, {}, { Icon(Icons.Default.AccountBalance, null) }, label = { Text("Mutual") })
        NavigationBarItem(false, {}, { Icon(Icons.Default.Payments, null) }, label = { Text("UPI") })
    }
}

/* =========================================================
   UI COMPONENTS
   ========================================================= */

@Composable
fun MarketStatusBadge(status: MarketStatusResponse?) {
    if (status == null) return
    val (text, color) = when (status.status) {
        "OPEN" -> "Market Open" to Color(0xFF22C55E)
        "CLOSED" -> "Market Closed" to Color(0xFFEF4444)
        else -> "Pre Open" to Color(0xFFFACC15)
    }

    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(text, modifier = Modifier.padding(8.dp), color = color)
    }
}

@Composable
fun RowScope.IndexCard(index: IndexItem) {
    val color by animateColorAsState(
        if (index.change >= 0) Color(0xFF22C55E) else Color(0xFFEF4444)
    )

    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF111827))
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(index.name, color = Color.White)
            Text("₹${index.price}", color = Color.White, fontWeight = FontWeight.Bold)
            Text("${index.change} (${index.percent}%)", color = color)
        }
    }
}

/* =========================================================
   GROWW-STYLE STOCK ROW (CORE)
   ========================================================= */

@Composable
fun GrowwStockRow(
    stock: StockItem,
    onClick: () -> Unit
) {
    val isPositive = stock.change >= 0
    val changeColor = if (isPositive) Color(0xFF22C55E) else Color(0xFFEF4444)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column {
            Text(stock.symbol, color = Color.White, fontWeight = FontWeight.SemiBold)
            Text(stock.name, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text("₹${stock.price}", color = Color.White, fontWeight = FontWeight.Medium)
            Text("${stock.change} (${stock.percent}%)", color = changeColor)
        }
    }

    Divider(color = Color(0xFF1F2937))
}
