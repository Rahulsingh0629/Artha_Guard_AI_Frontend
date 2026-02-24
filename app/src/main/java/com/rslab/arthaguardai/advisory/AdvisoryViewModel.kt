package com.rslab.arthaguardai.advisory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.rslab.arthaguardai.network.AdvisoryInstantAdviceRequest
import com.rslab.arthaguardai.network.AdvisoryProfileUpdateRequest
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

    fun fetchInvestmentPlan() {
        val sessionId = _uiState.value.activeSessionId
        if (sessionId.isBlank()) return

        appendMessageToSession(
            sessionId = sessionId,
            message = ChatMessage("Generate my investment plan based on my profile.", isUser = true)
        )
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val authHeader = buildAuthHeaderOrNull()
                if (authHeader == null) {
                    val errorMessage = "Session expired. Please login again."
                    appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    return@launch
                }

                val response = RetrofitInstance.api.getAdvisoryPlan(authHeader)
                appendMessageToSession(sessionId, ChatMessage(formatPlanResponse(response), isUser = false))
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = "Failed to fetch plan: ${e.message}"
                appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun requestInstantAdvice(input: InstantAdviceInput) {
        val sessionId = _uiState.value.activeSessionId
        if (sessionId.isBlank()) return

        appendMessageToSession(
            sessionId = sessionId,
            message = ChatMessage(input.question.trim(), isUser = true)
        )
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getInstantAdvice(
                    AdvisoryInstantAdviceRequest(
                        age = input.age,
                        annual_income = input.annualIncome,
                        monthly_savings = input.monthlySavings,
                        financial_goal = input.financialGoal,
                        time_horizon_years = input.timeHorizonYears,
                        question = input.question.trim()
                    )
                )
                appendMessageToSession(sessionId, ChatMessage(formatInstantAdviceResponse(response), isUser = false))
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = "Failed to fetch instant advice: ${e.message}"
                appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
            }
        }
    }

    fun updateProfile(input: ProfileUpdateInput) {
        val sessionId = _uiState.value.activeSessionId
        if (sessionId.isBlank()) return

        appendMessageToSession(
            sessionId = sessionId,
            message = ChatMessage(
                text = "Update profile:\nAge ${input.age}, Income ${input.annualIncome}, Savings ${input.monthlySavings}, Goal ${input.financialGoal}",
                isUser = true
            )
        )
        _uiState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val authHeader = buildAuthHeaderOrNull()
                if (authHeader == null) {
                    val errorMessage = "Session expired. Please login again."
                    appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                    _uiState.update { it.copy(isLoading = false, error = errorMessage) }
                    return@launch
                }

                val response = RetrofitInstance.api.updateProfile(
                    token = authHeader,
                    request = AdvisoryProfileUpdateRequest(
                        age = input.age,
                        annual_income = input.annualIncome,
                        monthly_savings = input.monthlySavings,
                        risk_appetite = input.riskAppetite,
                        financial_goal = input.financialGoal,
                        time_horizon_years = input.timeHorizonYears
                    )
                )
                val status = response.stringValue("status") ?: "Profile updated successfully."
                appendMessageToSession(
                    sessionId = sessionId,
                    message = ChatMessage("$status\nYou can now tap Plan to regenerate your personalized plan.", isUser = false)
                )
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                val errorMessage = "Failed to update profile: ${e.message}"
                appendMessageToSession(sessionId, ChatMessage(errorMessage, isUser = false))
                _uiState.update { it.copy(isLoading = false, error = errorMessage) }
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

    private fun buildAuthHeaderOrNull(): String? {
        val token = sessionManager.fetchAuthToken() ?: return null
        return if (token.trim().startsWith("Bearer", ignoreCase = true)) {
            token
        } else {
            "Bearer $token"
        }
    }

    private fun formatPlanResponse(response: JsonObject): String {
        val riskCategory = response.obj("ai_analysis")?.stringValue("risk_category").orEmpty()
        val explanation = response.stringValue("advisory_explanation").orEmpty()
        val actionPlan = response.array("action_plan")
            ?.takeAsStrings(limit = 4)
            ?.joinToString(separator = "\n") { "- $it" }
            .orEmpty()
        val allocation = response.obj("recommended_portfolio")
            ?.entrySet()
            ?.filter { it.key != "Strategy" }
            ?.joinToString(separator = "\n") { "${it.key}: ${it.value.asStringSafe()}" }
            .orEmpty()

        return buildString {
            append("Investment Plan")
            if (riskCategory.isNotBlank()) append("\nRisk Profile: $riskCategory")
            if (explanation.isNotBlank()) append("\n\n$explanation")
            if (allocation.isNotBlank()) append("\n\nRecommended Allocation:\n$allocation")
            if (actionPlan.isNotBlank()) append("\n\nAction Plan:\n$actionPlan")
        }.trim()
    }

    private fun formatInstantAdviceResponse(response: JsonObject): String {
        val riskProfile = response.obj("analysis")?.stringValue("risk_profile").orEmpty()
        val suggestedPortfolio = response.obj("analysis")?.obj("suggested_portfolio")
            ?.entrySet()
            ?.filter { it.key != "Strategy" }
            ?.joinToString(separator = "\n") { "${it.key}: ${it.value.asStringSafe()}" }
            .orEmpty()
        val aiAdvice = response.stringValue("ai_advice").orEmpty()

        return buildString {
            append("Instant Advice")
            if (riskProfile.isNotBlank()) append("\nRisk Profile: $riskProfile")
            if (aiAdvice.isNotBlank()) append("\n\n$aiAdvice")
            if (suggestedPortfolio.isNotBlank()) append("\n\nSuggested Portfolio:\n$suggestedPortfolio")
        }.trim()
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
        return value.asStringSafe()
    }

    private fun JsonArray.takeAsStrings(limit: Int): List<String> {
        return take(limit).mapNotNull { it.asStringSafe() }
    }

    private fun com.google.gson.JsonElement.asStringSafe(): String? {
        return runCatching {
            when {
                isJsonNull -> null
                isJsonPrimitive -> asJsonPrimitive.asString
                else -> toString()
            }
        }.getOrNull()
    }

    private fun String.toPreview(max: Int = 58): String {
        val compact = replace("\n", " ").trim()
        return if (compact.length <= max) compact else "${compact.take(max)}..."
    }
}
