package com.rslab.arthaguardai

import android.net.Uri
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rslab.arthaguardai.advisory.AdvisoryScreen
import com.rslab.arthaguardai.auth.login.LoginScreen
import com.rslab.arthaguardai.auth.register.RegisterScreen
import com.rslab.arthaguardai.home.AllMarketListScreen
import com.rslab.arthaguardai.home.HomeScreen
import com.rslab.arthaguardai.home.StockDetailScreen
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
    val startDest = if (token != null) "home" else "login"

    NavHost(navController = navController, startDestination = startDest) {

        composable("login") {
            LoginScreen(navController = navController)
        }

        composable("register") {
            RegisterScreen(navController = navController)
        }

        composable("home") {
            HomeScreen(
                onOpenAdvisory = { navController.navigate("advisory") },
                onOpenAllStocks = { navController.navigate("all_market/stocks") },
                onOpenAllMovers = { navController.navigate("all_market/movers") },
                onOpenStockDetail = { symbol ->
                    navController.navigate("stock_detail/${Uri.encode(symbol)}")
                },
                onLogout = {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                },
            )
        }

        composable("all_market/{mode}") { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "stocks"
            AllMarketListScreen(
                mode = mode,
                onBack = { navController.popBackStack() },
                onStockClick = { symbol ->
                    navController.navigate("stock_detail/${Uri.encode(symbol)}")
                }
            )
        }

        composable(
            route = "stock_detail/{symbol}",
            arguments = listOf(
                navArgument("symbol") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val symbol = backStackEntry.arguments?.getString("symbol").orEmpty()
            StockDetailScreen(
                symbol = symbol,
                onBack = { navController.popBackStack() }
            )
        }

        composable("advisory") {
            AdvisoryScreen(
                onLogoutClick = {
                    sessionManager.logout()
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }
    }
}
