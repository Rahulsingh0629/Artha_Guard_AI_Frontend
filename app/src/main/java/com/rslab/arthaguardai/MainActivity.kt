package com.rslab.arthaguardai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

import com.rslab.arthaguardai.auth.login.LoginScreen
import com.rslab.arthaguardai.auth.register.RegisterScreen
import com.rslab.arthaguardai.advisory.AdvisoryScreen // Import the new screen
import com.rslab.arthaguardai.utils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ArthaGuardNavHost()
                }
            }
        }
    }
}

@Composable
fun ArthaGuardNavHost() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val token = sessionManager.fetchAuthToken()

    // 👇 LOGIC CHANGE: Check session and go straight to ADVISORY
    val startDest = if (token != null) "advisory" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        // 👇 NEW MAIN DASHBOARD (The Advisor)
        composable("advisory") {
            AdvisoryScreen(
                onLogoutClick = {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo("advisory") { inclusive = true }
                    }
                }
            )
        }
    }
}