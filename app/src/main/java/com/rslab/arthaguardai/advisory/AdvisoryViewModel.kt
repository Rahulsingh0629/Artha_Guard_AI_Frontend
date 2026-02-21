package com.rslab.arthaguardai.advisory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.ChatRequest
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AdvisoryViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AdvisoryUiState())
    val uiState = _uiState.asStateFlow()

    private val sessionManager = SessionManager(application)
    private val sessionMessages = mutableMapOf<String, MutableList<ChatMessage>>()

    init {
        val defaultAgents = listOf(
            AdvisoryAgent(
                id = "advisory",
                name = "Advisory AI",
                description = "Stocks, portfolio strategy, and long-term planning."
            ),
            AdvisoryAgent(
                id = "fraud_guard",
                name = "Fraud Guard",
                description = "Scam detection, UPI safety, and risk checks."
            ),
            AdvisoryAgent(
                id = "market_scanner",
                name = "Market Scanner",
                description = "Momentum watchlist and stock signal ideas."
            ),
            AdvisoryAgent(
                id = "tax_planner",
                name = "Tax Planner",
                description = "Tax-efficient investing and deduction guidance."
            )
        )

        val firstSessionId = createSessionId()
        val firstWelcomeMessage = createWelcomeMessage(defaultAgents.first().name)
        sessionMessages[firstSessionId] = mutableListOf(firstWelcomeMessage)

        _uiState.value = AdvisoryUiState(
            activeSessionId = firstSessionId,
            sessions = listOf(
                ChatSessionSummary(
                    id = firstSessionId,
                    title = "New chat",
                    preview = firstWelcomeMessage.text.toPreview(),
                    updatedAt = firstWelcomeMessage.timestamp
                )
            ),
            messages = listOf(firstWelcomeMessage),
            agents = defaultAgents,
            selectedAgentId = defaultAgents.first().id
        )
    }

    fun startNewChat() {
        val currentState = _uiState.value
        val selectedAgent = currentState.agents.firstOrNull { it.id == currentState.selectedAgentId } ?: return

        val sessionId = createSessionId()
        val welcomeMessage = createWelcomeMessage(selectedAgent.name)
        sessionMessages[sessionId] = mutableListOf(welcomeMessage)

        val newSummary = ChatSessionSummary(
            id = sessionId,
            title = "New chat",
            preview = welcomeMessage.text.toPreview(),
            updatedAt = welcomeMessage.timestamp
        )

        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                sessions = (listOf(newSummary) + it.sessions).sortedByDescending { session -> session.updatedAt },
                messages = listOf(welcomeMessage),
                isLoading = false,
                error = null
            )
        }
    }

    fun selectChatSession(sessionId: String) {
        val targetMessages = sessionMessages[sessionId] ?: return
        _uiState.update {
            it.copy(
                activeSessionId = sessionId,
                messages = targetMessages.toList(),
                error = null
            )
        }
    }

    fun selectAgent(agentId: String) {
        val currentState = _uiState.value
        if (currentState.selectedAgentId == agentId) return

        val agent = currentState.agents.firstOrNull { it.id == agentId } ?: return
        _uiState.update { it.copy(selectedAgentId = agentId) }

        appendMessageToSession(
            sessionId = _uiState.value.activeSessionId,
            message = ChatMessage(
                text = "Switched to ${agent.name}. Ask anything about ${agent.description}",
                isUser = false
            )
        )
    }

    fun sendMessage(userMessage: String) {
        val message = userMessage.trim()
        if (message.isBlank()) return

        val sessionId = _uiState.value.activeSessionId
        if (sessionId.isBlank()) return

        appendMessageToSession(
            sessionId = sessionId,
            message = ChatMessage(message, isUser = true)
        )

        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    val errorMessage = "Session expired. Please login again."
                    appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    return@launch
                }

                val authHeader = if (token.trim().startsWith("Bearer", ignoreCase = true)) {
                    token
                } else {
                    "Bearer $token"
                }

                val response = RetrofitInstance.api.chatWithAdvisor(authHeader, ChatRequest(message))
                appendMessageToSession(sessionId, ChatMessage(response.response, isUser = false))
                _uiState.update { it.copy(isLoading = false) }

            } catch (e: Exception) {
                val errorMsg = if (e.message?.contains("401") == true) {
                    "Session expired. Please logout and login again."
                } else {
                    "Connection error: ${e.message}. Please try again."
                }

                appendMessageToSession(sessionId, ChatMessage(errorMsg, isUser = false))
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }

    private fun appendMessageToSession(sessionId: String, message: ChatMessage) {
        val messagesForSession = sessionMessages.getOrPut(sessionId) { mutableListOf() }
        messagesForSession.add(message)

        _uiState.update { currentState ->
            val existingSummary = currentState.sessions.firstOrNull { it.id == sessionId }
            val updatedSummary = buildSessionSummary(
                sessionId = sessionId,
                messages = messagesForSession,
                fallbackTitle = existingSummary?.title ?: "New chat"
            )

            val updatedSessions = (currentState.sessions.filterNot { it.id == sessionId } + updatedSummary)
                .sortedByDescending { it.updatedAt }

            currentState.copy(
                sessions = updatedSessions,
                messages = if (currentState.activeSessionId == sessionId) {
                    messagesForSession.toList()
                } else {
                    currentState.messages
                }
            )
        }
    }

    private fun buildSessionSummary(
        sessionId: String,
        messages: List<ChatMessage>,
        fallbackTitle: String
    ): ChatSessionSummary {
        val firstUserMessage = messages.firstOrNull { it.isUser }?.text?.trim().orEmpty()
        val title = if (firstUserMessage.isBlank()) fallbackTitle else firstUserMessage.take(36)
        val latestMessage = messages.lastOrNull()

        return ChatSessionSummary(
            id = sessionId,
            title = title,
            preview = latestMessage?.text?.toPreview() ?: "No messages yet.",
            updatedAt = latestMessage?.timestamp ?: System.currentTimeMillis()
        )
    }

    private fun createWelcomeMessage(agentName: String): ChatMessage {
        return ChatMessage(
            text = "Hello! I am $agentName.\n\nAsk anything about stocks, portfolio, risk, or financial planning.",
            isUser = false
        )
    }

    private fun createSessionId(): String {
        return "chat_${System.currentTimeMillis()}_${(1000..9999).random()}"
    }

    private fun String.toPreview(max: Int = 58): String {
        val compact = replace("\n", " ").trim()
        return if (compact.length <= max) compact else "${compact.take(max)}..."
    }
}
