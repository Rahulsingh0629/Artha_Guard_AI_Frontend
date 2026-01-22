package com.rslab.arthaguardai.auth.login

import androidx.lifecycle.ViewModel
import com.rslab.arthaguardai.api.LoginRequest
import com.rslab.arthaguardai.api.LoginResponse
import com.rslab.arthaguardai.api.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    suspend fun login(email: String, pass: String) {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        val request = LoginRequest(email = email, password = pass
        )

        RetrofitInstance.api.login(request).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _uiState.update { it.copy(isLoading = false, isSuccess = true, userEmail = email) }
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: ${response.code()}") }
                }
            }
            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Failed: ${t.message}") }
            }
        })
    }

    fun resetState() {
        _uiState.update { LoginUiState() }
    }
}