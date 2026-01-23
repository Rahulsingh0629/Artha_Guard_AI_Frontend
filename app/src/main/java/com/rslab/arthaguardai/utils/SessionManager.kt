package com.rslab.arthaguardai.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private var prefs: SharedPreferences = context.getSharedPreferences("user_session", Context.MODE_PRIVATE)

    companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_USER_EMAIL = "user_email"
    }

    /**
     * Function to save auth token
     */
    fun saveAuthToken(token: String, email: String) {
        val editor = prefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, token)
        editor.putString(KEY_USER_EMAIL, email)
        editor.apply()
    }

    /**
     * Function to fetch auth token
     */
    fun fetchAuthToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Function to fetch user email
     */
    fun fetchUserEmail(): String? {
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    /**
     * Clear session (Logout)
     */
    fun logout() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}