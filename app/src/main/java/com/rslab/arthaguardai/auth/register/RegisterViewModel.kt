package com.rslab.arthaguardai.auth.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rslab.arthaguardai.network.RegisterRequest
import com.rslab.arthaguardai.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel : ViewModel() {

    private val api = RetrofitInstance.api

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(
        fullName: String,
        userName: String,
        email: String,
        password: String,
        phone: String
    ) {
        _uiState.update {
            it.copy(
                isLoading = true,
                errorMessage = null,
                isSuccess = false
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = api.register(
                    RegisterRequest(
                        full_name = fullName,
                        username = userName,
                        email = email,
                        password = password,
                        phone_number = phone
                    )
                )

                if (response.isSuccessful) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = true
                        )
                    }
                } else {
                    val serverMessage = extractErrorMessage(response.errorBody()?.string())
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isSuccess = false,
                            errorMessage = serverMessage ?: "Registration failed (${response.code()})"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Network error"
                    )
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = RegisterUiState()
    }

    private fun extractErrorMessage(raw: String?): String? {
        if (raw.isNullOrBlank()) return null
        val detail = """"detail"\s*:\s*"([^"]+)"""".toRegex().find(raw)?.groupValues?.getOrNull(1)
        val message = """"message"\s*:\s*"([^"]+)"""".toRegex().find(raw)?.groupValues?.getOrNull(1)
        return detail ?: message ?: raw
    }
}
