package com.rslab.arthaguardai.advisory

data class AdvisoryUiState(
    val activeSessionId: String = "",
    val sessions: List<ChatSessionSummary> = emptyList(),
    val messages: List<ChatMessage> = emptyList(),
    val agents: List<AdvisoryAgent> = emptyList(),
    val selectedAgentId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

data class AdvisoryAgent(
    val id: String,
    val name: String,
    val description: String
)

data class ChatSessionSummary(
    val id: String,
    val title: String,
    val preview: String,
    val updatedAt: Long
)

data class ChatMessage(
    val text: String,
    val isUser: Boolean, // true = User, false = AI
    val timestamp: Long = System.currentTimeMillis()
)
