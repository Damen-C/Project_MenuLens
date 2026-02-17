package com.menulens.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppScreen(
    title: String,
    subtitle: String,
    centerContent: Boolean = false,
    showBrandAsBlock: Boolean = false,
    showHeaderCard: Boolean = true,
    topPadding: Dp = 20.dp,
    content: @Composable () -> Unit
) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFEFFFFF),
            Color(0xFFE8FFE9),
            Color(0xFFFFF7D6)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        Bubble(
            size = 180.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.16f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 24.dp, end = 8.dp)
        )
        Bubble(
            size = 140.dp,
            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.20f),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 4.dp, bottom = 20.dp)
        )

        Column(
            modifier = Modifier
                .statusBarsPadding()
                .fillMaxSize()
                .padding(start = 20.dp, top = topPadding, end = 20.dp, bottom = 20.dp),
            verticalArrangement = if (centerContent) {
                Arrangement.spacedBy(20.dp, Alignment.CenterVertically)
            } else {
                Arrangement.spacedBy(16.dp)
            }
        ) {
            if (showBrandAsBlock) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Text(
                        text = "MenuLens",
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp)
                    )
                }
            }

            if (showHeaderCard) {
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle.isNotBlank()) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                            )
                        }
                    }
                }
            }

            content()
        }
    }
}

@Composable
private fun Bubble(size: Dp, color: Color, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = color, shape = CircleShape)
    )
}
