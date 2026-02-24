package com.rslab.arthaguardai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

val HomeBackgroundBrush: Brush = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFF3F9FF),
        Color(0xFFE5F0FF),
        Color(0xFFDAE8FF)
    )
)

@Composable
fun HomeStyleTopBar(
    title: String = "",
    subtitle: String? = null,
    navigationIcon: ImageVector,
    navigationContentDescription: String,
    onNavigationClick: () -> Unit,
    primaryActionIcon: ImageVector? = null,
    primaryActionContentDescription: String = "",
    onPrimaryActionClick: (() -> Unit)? = null,
    secondaryActionIcon: ImageVector = Icons.Default.AccountCircle,
    secondaryActionContentDescription: String = "Profile",
    onSecondaryActionClick: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color(0xFF01040E),
        shadowElevation = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 40.dp, bottom = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigationClick) {
                Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                    Icon(
                        imageVector = navigationIcon,
                        contentDescription = navigationContentDescription,
                        tint = Color.White,
                        modifier = Modifier.padding(6.dp)
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (!subtitle.isNullOrBlank()) {
                    Text(
                        text = subtitle,
                        color = Color(0xFF93C5FD),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (primaryActionIcon != null && onPrimaryActionClick != null) {
                    IconButton(onClick = onPrimaryActionClick) {
                        Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.15f)) {
                            Icon(
                                imageVector = primaryActionIcon,
                                contentDescription = primaryActionContentDescription,
                                tint = Color.White,
                                modifier = Modifier.padding(6.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = { onSecondaryActionClick?.invoke() }) {
                    Icon(
                        imageVector = secondaryActionIcon,
                        contentDescription = secondaryActionContentDescription,
                        tint = Color.White
                    )
                }
            }
        }
    }
}
