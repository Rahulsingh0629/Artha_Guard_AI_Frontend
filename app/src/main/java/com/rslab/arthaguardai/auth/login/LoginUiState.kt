package com.rslab.arthaguardai.auth.login

data class LoginUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val errorMessage: String? = null,
    val userEmail: String = "" // Store email to pass to dashboard
)