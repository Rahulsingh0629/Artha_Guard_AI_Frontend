package com.rslab.arthaguardai.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(userName: String, navController: NavHostController) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()
        val viewModel: DashboardViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DashboardDrawer(
                    onItemClick = { scope.launch { drawerState.close()
                    }
                    }
                )
            }
        )  {
            Scaffold (
                containerColor = Color(0xFF287CA1),
                topBar = {
                    DashboardTopBar(
                        onMenuClick = {
                            scope.launch {drawerState.open()}
                        },
                        onSearchClick = {
                            TODO()
                        },
                        onProfileClick = {
                            TODO()
                        }
                    )
                },
                bottomBar = {
                    BottomNavigationBar()
                }
            ){ padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ){
                    DashboardTabs()

                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        IndexCard(
                            title = "NIFTY 50",
                            change = uiState.niftyChange,
                            isPositive = uiState.niftyPositive,
                            value = uiState.niftyValue,
                            isNegative = !uiState.niftyPositive
                        )
                        IndexCard(
                            title = "SENSEX",
                            value = uiState.sensexValue,
                            change = uiState.sensexChange,
                            isPositive = uiState.sensexPositive,
                            isNegative = !uiState.sensexPositive
                        )
                    }

                        SectionTitle("Recently viewed")
                        RecentlyViewedRow()

                        SectionTitle("Products and tools")
                        ProductsToolsGrid()

                        SectionTitle("Top movers today")
                        TopMoversRow()

                        SectionTitle("Trading setups")
                        TradingSetupsGrid()

                        Spacer(modifier = Modifier.height(80.dp))


                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp),
        color = Color(0xFFF3F4F6),
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE2E5EE),
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black,
                actionIconContentColor = Color.Black
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "ArthaGuard",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            navigationIcon ={
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    )
                }
            }
        )
    }
}

@Composable
fun DashboardDrawer(onItemClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        shape = RoundedCornerShape(
            topEnd = 24.dp,
            bottomEnd = 24.dp
        ),
        color = Color(0xFF9A9FAF),
        shadowElevation = 8.dp
    ) {
        Column (
            modifier = Modifier
                .padding(16.dp)
        ){
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 35.dp, start = 15.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            DrawerItem(
                title = "Dashboard",
                onClick = onItemClick
            )
            DrawerItem(title = "Profile", onClick = onItemClick)
            DrawerItem(title = "Portfolio", onClick = onItemClick)
            DrawerItem(title = "Scanner", onClick = onItemClick)
            DrawerItem(title = "Advisory AI", onClick = onItemClick)
            DrawerItem(title = "News", onClick = onItemClick)
            DrawerItem(title = "Fraud Check", onClick = onItemClick)
            DrawerItem(title = "Settings", onClick = onItemClick)
            DrawerItem(title = "Logout", onClick = onItemClick)

        }
    }
}
@Composable
fun DrawerItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun TradingSetupsGrid() {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SetupCard("Resistance breakouts", true)
            SetupCard("RSI overbought", false)
        }
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SetupCard("MACD above signal", true)
            SetupCard("RSI oversold", true)
        }
    }
}

@Composable
fun SetupCard(title: String, bullish: Boolean) {
    Surface(
        modifier = Modifier.width(170.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111827)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (bullish) "Bullish" else "Bearish",
                color = if (bullish) Color(0xFF22C55E) else Color(0xFFEF4444)
            )
            Spacer(Modifier.height(8.dp))
            Text(title, color = Color.White)
        }
    }
}

@Composable
fun TopMoversRow() {
    Row(
        modifier = Modifier
            .horizontalScroll(rememberScrollState())
            .padding(start = 16.dp)
    ) {
        listOf("Mazagon Dock", "Tata Steel").forEach {
            StockLargeCard(it)
        }
    }
}

@Composable
fun StockLargeCard(name: String) {
    Surface(
        modifier = Modifier
            .width(180.dp)
            .padding(end = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111827)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(name, color = Color.White)
            Text("+10.58%", color = Color(0xFF22C55E))
        }
    }
}

@Composable
fun ProductsToolsGrid() {
    val items = listOf("MTF", "Events", "ETF", "IPO", "Bonds", "Screener", "Intraday", "SIP")

    Column {
        items.chunked(4).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFF22C55E),
                            modifier = Modifier.size(32.dp)
                        )
                        Text(it, color = Color.White, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RecentlyViewedRow() {
    Row(
        modifier = Modifier.padding(start = 16.dp)
            .horizontalScroll(rememberScrollState())
    ) {
        listOf("TCS", "TATAPOWER", "MEESHO", "PWL").forEach {
            StockMiniCard(it)
        }
    }
}

@Composable
fun StockMiniCard(name: String) {
    Surface(
        modifier = Modifier.width(120.dp)
            .padding(end = 12.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111827)
    ) {
        Column (
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ){
            Text(name, color = Color.White)
            Text("-0.76%", color = Color.Red)
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        color = Color.White,
        fontWeight = FontWeight.Medium,
        modifier = Modifier.padding(16.dp)
    )
}

@Composable
fun RowScope.IndexCard(
    title: String,
    change: String,
    isPositive: Boolean,
    value: String,
    isNegative: Boolean
) {
    Surface(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF111827)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            val changeColor = if (isPositive) Color(0xFF10B981) else Color(0xFFEF4444)
            Text(
                text = change,
                color = changeColor,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DashboardTabs() {
    val tabs = listOf("Explore", "Holdings", "Positions", "Orders")
    var selected by remember { mutableStateOf(0) }

    TabRow(
        selectedTabIndex = selected,
        containerColor = Color.Black,
        contentColor = Color.White
    ){
        tabs.forEachIndexed{ index, tab ->
            Tab(
                selected = selected == index,
                onClick = {selected = index},
                text = {
                    Text(text = tab, fontWeight = FontWeight.Medium)
                }

            )
        }
    }
}

@Composable
fun BottomNavigationBar() {
    NavigationBar(containerColor = Color.Black) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.ShowChart,
                null) },
            label = { Text("Stocks") })
        NavigationBarItem(false, {}, { Icon(Icons.Default.TrendingUp, null) },
            label = { Text("F&O") }
        )
        NavigationBarItem(false, {}, { Icon(Icons.Default.AccountBalance, null) },
            label = { Text("Mutual") })
        NavigationBarItem(false, {}, { Icon(Icons.Default.Payments, null) },
            label = { Text("UPI") })
    }
}

