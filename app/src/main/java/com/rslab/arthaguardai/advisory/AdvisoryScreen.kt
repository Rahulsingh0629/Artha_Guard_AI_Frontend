package com.rslab.arthaguardai.advisory

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@Composable
fun AdvisoryScreen(
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdvisoryViewModel = viewModel()
    val uiState by viewModel.uiState.collectAsState()
    var inputText by remember { mutableStateOf("") }
    var pendingAttachment by remember { mutableStateOf<PendingAttachment?>(null) }
    val listState = rememberLazyListState()
    val drawerState = rememberDrawerState(initialValue = androidx.compose.material3.DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val selectedAgentName = uiState.agents.firstOrNull { it.id == uiState.selectedAgentId }?.name ?: "Advisory AI"

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        pendingAttachment = uri?.let {
            PendingAttachment(
                uri = it,
                name = resolveDisplayName(context, it) ?: "photo.jpg",
                isImage = true
            )
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        pendingAttachment = uri?.let {
            PendingAttachment(
                uri = it,
                name = resolveDisplayName(context, it) ?: "document",
                isImage = false
            )
        }
    }

    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = Color(0xFF111827),
                drawerContentColor = Color.White
            ) {
                AdvisoryDrawerContent(
                    agents = uiState.agents,
                    selectedAgentId = uiState.selectedAgentId,
                    sessions = uiState.sessions,
                    activeSessionId = uiState.activeSessionId,
                    onNewChatClick = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onAgentClick = { agentId ->
                        viewModel.selectAgent(agentId)
                        scope.launch { drawerState.close() }
                    },
                    onSessionClick = { sessionId ->
                        viewModel.selectChatSession(sessionId)
                        scope.launch { drawerState.close() }
                    },
                    onLogoutClick = {
                        scope.launch { drawerState.close() }
                        onLogoutClick()
                    }
                )
            }
        }
    ) {
        Scaffold(
            containerColor = Color(0xFF030712),
            topBar = {
                AdvisoryTopBar(
                    selectedAgentName = selectedAgentName,
                    onMenuClick = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = {
                ChatInputArea(
                    text = inputText,
                    onTextChanged = { inputText = it },
                    onSendClick = {
                        val userMessage = buildUserMessage(
                            text = inputText,
                            attachment = pendingAttachment
                        )
                        if (userMessage.isNotBlank() && !uiState.isLoading) {
                            viewModel.sendMessage(userMessage)
                            inputText = ""
                            pendingAttachment = null
                        }
                    },
                    isLoading = uiState.isLoading,
                    attachmentName = pendingAttachment?.name,
                    onRemoveAttachmentClick = { pendingAttachment = null },
                    onUploadPhotoClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onUploadFileClick = { filePickerLauncher.launch(arrayOf("*/*")) }
                )
            }
        ) { padding ->
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.messages) { message ->
                    MessageBubble(message = message)
                }

                if (uiState.isLoading) {
                    item { TypingIndicator() }
                }
            }
        }
    }
}

@Composable
private fun AdvisoryDrawerContent(
    agents: List<AdvisoryAgent>,
    selectedAgentId: String,
    sessions: List<ChatSessionSummary>,
    activeSessionId: String,
    onNewChatClick: () -> Unit,
    onAgentClick: (String) -> Unit,
    onSessionClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "ArthaGuard AI",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Advanced advisory workspace",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))

        NavigationDrawerItem(
            label = { Text("New chat", fontWeight = FontWeight.Medium) },
            selected = false,
            onClick = onNewChatClick,
            icon = { Icon(Icons.Default.Add, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color(0xFF1F2937),
                unselectedIconColor = Color(0xFF2DD4BF),
                unselectedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF374151))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "My Agents",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        agents.forEach { agent ->
            NavigationDrawerItem(
                label = {
                    Column {
                        Text(
                            text = agent.name,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = agent.description,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 12.sp,
                            color = Color(0xFF9CA3AF)
                        )
                    }
                },
                selected = selectedAgentId == agent.id,
                onClick = { onAgentClick(agent.id) },
                icon = {
                    Icon(
                        imageVector = agentIcon(agent.id),
                        contentDescription = null
                    )
                },
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = Color(0xFF1F2937),
                    selectedIconColor = Color(0xFF2DD4BF),
                    selectedTextColor = Color.White,
                    unselectedContainerColor = Color.Transparent,
                    unselectedTextColor = Color.White,
                    unselectedIconColor = Color(0xFF9CA3AF)
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFF374151))
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Chat History",
            fontSize = 13.sp,
            color = Color(0xFF9CA3AF),
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (sessions.isEmpty()) {
            Text(
                text = "No chats yet.",
                color = Color(0xFF9CA3AF),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        } else {
            sessions.forEach { session ->
                NavigationDrawerItem(
                    label = {
                        Column {
                            Text(
                                text = session.title,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = session.preview,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp,
                                color = Color(0xFF9CA3AF)
                            )
                        }
                    },
                    selected = activeSessionId == session.id,
                    onClick = { onSessionClick(session.id) },
                    icon = { Icon(Icons.Default.ChatBubbleOutline, contentDescription = null) },
                    colors = NavigationDrawerItemDefaults.colors(
                        selectedContainerColor = Color(0xFF1F2937),
                        selectedIconColor = Color(0xFF2DD4BF),
                        selectedTextColor = Color.White,
                        unselectedContainerColor = Color.Transparent,
                        unselectedTextColor = Color.White,
                        unselectedIconColor = Color(0xFF9CA3AF)
                    ),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        HorizontalDivider(color = Color(0xFF374151))
        Spacer(modifier = Modifier.height(8.dp))

        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogoutClick,
            icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
            colors = NavigationDrawerItemDefaults.colors(
                unselectedContainerColor = Color.Transparent,
                unselectedIconColor = Color(0xFFF87171),
                unselectedTextColor = Color(0xFFFCA5A5)
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdvisoryTopBar(
    selectedAgentName: String,
    onMenuClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "ArthaGuard AI",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedAgentName,
                    fontSize = 12.sp,
                    color = Color(0xFF2DD4BF)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(Icons.Default.Menu, contentDescription = "Open menu")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color(0xFF111827),
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White
        ),
        modifier = Modifier.statusBarsPadding()
    )
}

@Composable
private fun MessageBubble(message: ChatMessage) {
    val isUser = message.isUser
    val align = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) Color(0xFF2DD4BF) else Color(0xFF1F2937)
    val textColor = if (isUser) Color.Black else Color.White
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 20.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomStart = 4.dp, bottomEnd = 20.dp)
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = align
    ) {
        AnimatedVisibility(
            visible = true,
            enter = slideInVertically(initialOffsetY = { 20 }) + fadeIn()
        ) {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                if (!isUser) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF374151)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SmartToy,
                            contentDescription = "AI",
                            tint = Color(0xFF2DD4BF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Surface(
                    color = containerColor,
                    shape = shape,
                    modifier = Modifier.widthIn(max = 300.dp)
                ) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatInputArea(
    text: String,
    onTextChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    isLoading: Boolean,
    attachmentName: String?,
    onRemoveAttachmentClick: () -> Unit,
    onUploadPhotoClick: () -> Unit,
    onUploadFileClick: () -> Unit
) {
    var attachmentMenuExpanded by remember { mutableStateOf(false) }

    Surface(
        color = Color(0xFF111827),
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (!attachmentName.isNullOrBlank()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 10.dp, end = 10.dp, bottom = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = null,
                            tint = Color(0xFF9CA3AF),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = attachmentName,
                            color = Color(0xFFE5E7EB),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f),
                            fontSize = 13.sp
                        )
                        TextButton(onClick = onRemoveAttachmentClick) {
                            Text("Remove", color = Color(0xFFFCA5A5), fontSize = 12.sp)
                        }
                    }
                }

                OutlinedTextField(
                    value = text,
                    onValueChange = onTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = "Message ArthaGuard AI",
                            color = Color(0xFF9CA3AF)
                        )
                    },
                    shape = RoundedCornerShape(26.dp),
                    maxLines = 4,
                    leadingIcon = {
                        Box {
                            IconButton(onClick = { attachmentMenuExpanded = true }) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Attach",
                                    tint = Color(0xFF2DD4BF)
                                )
                            }
                            DropdownMenu(
                                expanded = attachmentMenuExpanded,
                                onDismissRequest = { attachmentMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Upload photo") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        attachmentMenuExpanded = false
                                        onUploadPhotoClick()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Upload file") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AttachFile,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        attachmentMenuExpanded = false
                                        onUploadFileClick()
                                    }
                                )
                            }
                        }
                    },
                    trailingIcon = {
                        IconButton(
                            enabled = (text.isNotBlank() || !attachmentName.isNullOrBlank()) && !isLoading,
                            onClick = onSendClick
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color(0xFF2DD4BF),
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = if (text.isNotBlank() || !attachmentName.isNullOrBlank()) Color(0xFF2DD4BF) else Color(0xFF6B7280)
                                )
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if ((text.isNotBlank() || !attachmentName.isNullOrBlank()) && !isLoading) {
                                onSendClick()
                            }
                        }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2DD4BF),
                        unfocusedBorderColor = Color(0xFF374151),
                        focusedContainerColor = Color(0xFF030712),
                        unfocusedContainerColor = Color(0xFF030712),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
            }
        }
    }
}

@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing")
    val alpha by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "typing_alpha"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 8.dp, top = 4.dp)
    ) {
        Text(
            text = "AI thinking",
            color = Color(0xFF9CA3AF),
            fontSize = 13.sp
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(".", color = Color(0xFF9CA3AF), modifier = Modifier.alpha(alpha))
        Text(".", color = Color(0xFF9CA3AF), modifier = Modifier.alpha(alpha))
        Text(".", color = Color(0xFF9CA3AF), modifier = Modifier.alpha(alpha))
    }
}

private fun agentIcon(agentId: String): ImageVector {
    return when (agentId) {
        "advisory" -> Icons.Default.SmartToy
        "fraud_guard" -> Icons.Default.Security
        "market_scanner" -> Icons.AutoMirrored.Filled.ShowChart
        "tax_planner" -> Icons.AutoMirrored.Filled.ReceiptLong
        else -> Icons.Default.SmartToy
    }
}

private data class PendingAttachment(
    val uri: Uri,
    val name: String,
    val isImage: Boolean
)

private fun buildUserMessage(text: String, attachment: PendingAttachment?): String {
    val cleanText = text.trim()
    if (attachment == null) return cleanText

    val attachmentType = if (attachment.isImage) "photo" else "file"
    val attachmentLine = "[Attached $attachmentType: ${attachment.name}]"
    return if (cleanText.isBlank()) attachmentLine else "$cleanText\n$attachmentLine"
}

private fun resolveDisplayName(context: android.content.Context, uri: Uri): String? {
    return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (nameIndex >= 0 && cursor.moveToFirst()) cursor.getString(nameIndex) else null
    }
}
