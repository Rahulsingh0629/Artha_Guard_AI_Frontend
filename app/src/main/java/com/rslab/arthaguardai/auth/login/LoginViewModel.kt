package com.rslab.arthaguardai.auth.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.LoginRequest
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.utils.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val sessionManager = SessionManager(application)

    fun login(email: String, pass: String) {
        val cleanEmail = email.trim()
        val cleanPass = pass.trim()

        if (cleanEmail.isBlank() || cleanPass.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Email and password are required.") }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        val request = LoginRequest(email = cleanEmail, password = cleanPass)

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitInstance.api.login(request)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null && body.access_token.isNotBlank()) {
                        sessionManager.saveAuthToken(body.access_token, cleanEmail)
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                isSuccess = true,
                                userEmail = cleanEmail
                            )
                        }
                    } else {
                        _uiState.update {
                            it.copy(isLoading = false, errorMessage = "Invalid server response.")
                        }
                    }
                } else {
                    val serverMessage = extractErrorMessage(response.errorBody()?.string())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = serverMessage ?: "Login failed (${response.code()})."
                        )
                    }
                }
            } catch (t: Throwable) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = t.localizedMessage ?: "Network error during login."
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }

    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val detail = """"detail"\s*:\s*"([^"]+)"""".toRegex().find(raw)?.groupValues?.getOrNull(1)
        val message = """"message"\s*:\s*"([^"]+)"""".toRegex().find(raw)?.groupValues?.getOrNull(1)
        return detail ?: message ?: raw
    }
}
