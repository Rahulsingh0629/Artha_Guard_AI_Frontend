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

    init {
        // Initial Welcome Message
        _uiState.update {
            it.copy(messages = listOf(
                ChatMessage("Hello! I am ArthaGuard, your AI Wealth Manager.\n\nAsk me anything about stocks, your portfolio, or financial planning for the Indian market.", isUser = false)
            ))
        }
    }

    fun sendMessage(userMessage: String) {
        if (userMessage.isBlank()) return

        // 1. Add User Message immediately to UI
        val currentList = _uiState.value.messages.toMutableList()
        currentList.add(ChatMessage(userMessage, isUser = true))

        _uiState.update { it.copy(messages = currentList, isLoading = true) }

        viewModelScope.launch {
            try {
                // 2. Get Token
                val token = sessionManager.fetchAuthToken()
                if (token == null) {
                    _uiState.update { it.copy(isLoading = false, error = "Session expired. Please login again.") }
                    return@launch
                }

                // 3. 🛡️ ROBUST FIX: Check if "Bearer" is already there to avoid duplication
                // This prevents "Bearer Bearer eyJ..." errors
                val authHeader = if (token.trim().startsWith("Bearer", ignoreCase = true)) {
                    token // Already has prefix, use as is
                } else {
                    "Bearer $token" // Missing prefix, add it
                }

                // 4. Call Backend API
                val response = RetrofitInstance.api.chatWithAdvisor(authHeader, ChatRequest(userMessage))

                // 5. Add AI Response to UI
                val updatedList = _uiState.value.messages.toMutableList()
                updatedList.add(ChatMessage(response.response, isUser = false))

                _uiState.update { it.copy(messages = updatedList, isLoading = false) }

            } catch (e: Exception) {
                // Handle Error
                val errorMsg = if (e.message?.contains("401") == true) {
                    "Session expired. Please logout and login again."
                } else {
                    "Connection error: ${e.message}. Please try again."
                }

                val errorList = _uiState.value.messages.toMutableList()
                errorList.add(ChatMessage(errorMsg, isUser = false))
                _uiState.update { it.copy(messages = errorList, isLoading = false) }
            }
        }
    }
}