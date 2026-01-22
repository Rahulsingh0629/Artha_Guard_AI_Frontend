package com.rslab.arthaguardai

import android.R.attr.name
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.rslab.arthaguardai.auth.login.LoginScreen
import com.rslab.arthaguardai.auth.register.RegisterScreen
import com.rslab.arthaguardai.dashboard.DashboardScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Use MaterialTheme for colors and fonts
            MaterialTheme {
                // A surface container using the 'background' color from the theme
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
    // 1. Create the Navigation Controller
    val navController = rememberNavController()

    // 2. Define the Navigation Host (Start at "login")
    NavHost(navController = navController, startDestination = "login") {

        // --- ROUTE 1: LOGIN ---
        composable("login") {
            LoginScreen(navController = navController)
        }

        // --- ROUTE 2: REGISTER ---
        composable("register") {
            RegisterScreen(navController = navController)
        }

        // --- ROUTE 3: DASHBOARD ---
        // We define that this route expects an argument called "email"
        composable(
            route = "dashboard/{fullName}",
            arguments = listOf(navArgument("fullName") { type = NavType.StringType })
        ) { backStackEntry ->
            val fullName = backStackEntry.arguments?.getString("fullName") ?: ""
            DashboardScreen(userName = fullName)
        }
    }
}