package com.rslab.arthaguardai.advisory

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AdvisoryScreen(
    onLogoutClick: () -> Unit
) {
    val viewModel: AdvisoryViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Drawer State
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.size - 1)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF1F2937), // Dark Theme Color
                drawerContentColor = Color.White
            ) {
                // 👇 This makes the Drawer scrollable
                MenuDrawerContent(
                    onItemClick = { item ->
                        scope.launch { drawerState.close() }
                        if (item == "Logout") {
                            onLogoutClick()
                        }
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF111827), // Deep Charcoal Background
            topBar = {
                AdvisoryTopBar(
                    onMenuClick = { scope.launch { drawerState.open() } },
                    onLogoutClick = onLogoutClick
                )
            },
            bottomBar = {
                // Input area stays at the bottom
                ChatInputArea(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    onSendClick = {
                        viewModel.sendMessage(inputText)
                        inputText = ""
                    },
                    isLoading = uiState.isLoading
                )
            }
        ) { padding ->
            // Main Chat Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding) // Respects TopBar and BottomBar
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f) // Takes up all available space
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(uiState.messages) { message ->
                        MessageBubble(message)
                    }

                    if (uiState.isLoading) {
                        item {
                            TypingIndicator()
                        }
                    }
                }
            }
        }
    }
}

// --- 📜 SCROLLABLE DRAWER ---
@Composable
fun MenuDrawerContent(onItemClick: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()) // 👈 ADDED SCROLL HERE
    ) {
        // Drawer Header
        Text(
            text = "Menu",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        HorizontalDivider(color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))

        // Menu Items
        val menuItems = listOf(
            Triple("Profile", Icons.Default.Person, "Profile"),
            Triple("Advisory AI", Icons.Default.SmartToy, "Advisory AI"),
            Triple("Portfolio", Icons.Default.PieChart, "Portfolio"),
            Triple("Scanner", Icons.Default.Radar, "Scanner"),
            Triple("News", Icons.Default.Newspaper, "News"),
            Triple("Fraud Check", Icons.Default.Security, "Fraud Check"),
            Triple("Settings", Icons.Default.Settings, "Settings"),
            Triple("Logout", Icons.Default.ExitToApp, "Logout")
        )

        menuItems.forEach { (label, icon, action) ->
            NavigationDrawerItem(
                label = { Text(text = label, fontWeight = FontWeight.Medium) },
                selected = false,
                onClick = { onItemClick(action) },
                icon = { Icon(icon, contentDescription = null, tint = Color(0xFF2DD4BF)) },
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = Color.White
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Extra space at bottom for scrolling feel
        Spacer(modifier = Modifier.height(32.dp))
    }
}

// --- COMPONENTS ---

@Composable
fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val align = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) Color(0xFF2DD4BF) else Color(0xFF1F2937)
    val textColor = if (isUser) Color.Black else Color.White

    val shape = if (isUser) {
        RoundedCornerShape(20.dp, 20.dp, 0.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 0.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = align) {
        AnimatedVisibility(visible = true, enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn()) {
            Row(verticalAlignment = Alignment.Bottom) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.SmartToy, "AI", tint = Color(0xFF2DD4BF), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Surface(color = containerColor, shape = shape, modifier = Modifier.widthIn(max = 280.dp)) {
                    Text(text = message.text, color = textColor, modifier = Modifier.padding(12.dp), fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun AdvisoryTopBar(onMenuClick: () -> Unit, onLogoutClick: () -> Unit) {
    Surface(color = Color(0xFF1F2937), shadowElevation = 4.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onMenuClick) {
                    Icon(Icons.Default.Menu, "Menu", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text("ArthaGuard AI", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text("Wealth Manager", color = Color(0xFF2DD4BF), fontSize = 12.sp)
                }
            }
            IconButton(onClick = onLogoutClick) {
                Icon(Icons.Default.ExitToApp, "Logout", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ChatInputArea(text: String, onTextChanged: (String) -> Unit, onSendClick: () -> Unit, isLoading: Boolean) {
    Surface(color = Color(0xFF1F2937), tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .navigationBarsPadding()
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = text,
                onValueChange = onTextChanged,
                placeholder = { Text("Ask about Stocks...", color = Color.Gray) },
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 50.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF2DD4BF),
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF111827),
                    unfocusedContainerColor = Color(0xFF111827)
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(12.dp))
            FloatingActionButton(
                onClick = onSendClick,
                containerColor = if (text.isBlank() || isLoading) Color.Gray else Color(0xFF2DD4BF),
                shape = CircleShape,
                modifier = Modifier.size(50.dp)
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                else Icon(Icons.Default.Send, "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun TypingIndicator() {
    // 👇 PADDING UPDATED (Moved down by 24dp as requested)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 24.dp, top = 20.dp, bottom = 8.dp)
    ) {
        Text("AI Thinking", color = Color.Gray, fontSize = 18.sp)
        Spacer(modifier = Modifier.width(4.dp))
        val transition = rememberInfiniteTransition(label = "dots")
        val alpha by transition.animateFloat(
            initialValue = 0.2f, targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "alpha"
        )
        Text(".", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.alpha(alpha))
        Text(".", color = Color.Gray, fontSize = 12.sp, modifier = Modifier
            .padding(start = 2.dp)
            .alpha(alpha))
        Text(".", color = Color.Gray, fontSize = 12.sp, modifier = Modifier
            .padding(start = 2.dp)
            .alpha(alpha))
    }
}
