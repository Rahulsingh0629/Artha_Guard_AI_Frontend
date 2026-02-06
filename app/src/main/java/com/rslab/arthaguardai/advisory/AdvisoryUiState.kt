package com.rslab.arthaguardai.advisory

data class AdvisoryUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean, // true = User, false = AI
    val timestamp: Long = System.currentTimeMillis()
)