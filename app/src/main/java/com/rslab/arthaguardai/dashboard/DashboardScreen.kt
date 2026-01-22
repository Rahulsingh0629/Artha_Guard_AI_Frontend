package com.rslab.arthaguardai.dashboard

import android.R.attr.title
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.rslab.arthaguardai.ui.theme.ArthaGuardAITheme
import kotlinx.coroutines.launch


@Composable
fun DashboardScreen(userName: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ){
        val drawerState = rememberDrawerState(DrawerValue.Closed)
        val scope = rememberCoroutineScope()

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                DashboardDrawer(
                    onItemClick = { scope.launch { drawerState.close()
                    }
                    }
                )
            }
        )  {
            Scaffold (
                containerColor = Color(0xFFF3F4F6),
                topBar = {
                    DashboardTopBar(
                        userName = userName,
                        onMenuClick = {
                            scope.launch {drawerState.open()}
                        },
                        onSearchClick = {
                            TODO()
                        },
                        onProfileClick = {
                            TODO()
                        }
                    )
                }
            ){ padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(text = "Welcome to the Dashboard, $userName!")
                }
            }
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    userName: String,
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp, topStart = 16.dp, topEnd = 16.dp),
        color = Color(0xFFF3F4F6),
    ) {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color(0xFFE2E5EE),
                titleContentColor = Color.Black,
                navigationIconContentColor = Color.Black,
                actionIconContentColor = Color.Black
            ),
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "ArthaGuard",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = userName,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            navigationIcon ={
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu"
                    )
                }
            },
            actions = {
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search"
                    )
                }
                IconButton(onClick = onProfileClick) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Profile"
                    )
                }
            }
        )
    }
}

@Composable
fun DashboardDrawer(onItemClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp),
        shape = RoundedCornerShape(
            topEnd = 24.dp,
            bottomEnd = 24.dp
        ),
        color = Color(0xFF9A9FAF),
        shadowElevation = 8.dp
    ) {
        Column (
            modifier = Modifier
                .padding(16.dp)
        ){
            Text(
                text = "Menu",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp, top = 35.dp, start = 15.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))
            DrawerItem(
                title = "Dashboard",
                onClick = onItemClick
            )
            DrawerItem(title = "Profile", onClick = onItemClick)
            DrawerItem(title = "Portfolio", onClick = onItemClick)
            DrawerItem(title = "Scanner", onClick = onItemClick)
            DrawerItem(title = "Advisory AI", onClick = onItemClick)
            DrawerItem(title = "News", onClick = onItemClick)
            DrawerItem(title = "Fraud Check", onClick = onItemClick)
            DrawerItem(title = "Settings", onClick = onItemClick)
            DrawerItem(title = "Logout", onClick = onItemClick)

        }
    }
}
@Composable
fun DrawerItem(
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 15.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}


@Preview
@Composable
private fun DashboardScreenPreview() {
    ArthaGuardAITheme {
        DashboardScreen(userName = "John Doe")
    }
}

@Preview
@Composable
private fun DashBoardDrawerPreview() {
    ArthaGuardAITheme {
        DashboardDrawer(onItemClick = {})
    }

}

