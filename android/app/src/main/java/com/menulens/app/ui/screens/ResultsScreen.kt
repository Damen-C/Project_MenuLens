package com.menulens.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.menulens.app.BuildConfig
import com.menulens.app.model.MenuItem
import com.menulens.app.model.MenuPreview
import com.menulens.app.viewmodel.ResultsUiState
import kotlin.random.Random

@Composable
fun ResultsScreen(
    state: ResultsUiState,
    onItemTap: (String) -> Unit
) {
    val useDebugMockItems = BuildConfig.DEBUG && state.items.isEmpty()
    val items = if (useDebugMockItems) debugResultsItems else state.items

    AppScreen(
        title = "",
        subtitle = "",
        showHeaderCard = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            SakuraBackgroundDecoration()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Scan Results",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap the cards to show details of the dish.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                    )
                }

                items.forEach { item ->
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                width = 2.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(24.dp)
                            )
                            .clickable { onItemTap(item.itemId) }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = item.preview.enTitle,
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.jpText,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private val debugResultsItems = listOf(
    MenuItem(
        itemId = "debug_1",
        jpText = "いなり寿司",
        priceText = "¥350",
        preview = MenuPreview(
            enTitle = "Inari Sushi",
            enDescription = "Sweet tofu pockets filled with sushi rice.",
            tags = listOf("Sushi"),
            images = emptyList()
        )
    ),
    MenuItem(
        itemId = "debug_2",
        jpText = "天ぷらうどん",
        priceText = "¥980",
        preview = MenuPreview(
            enTitle = "Tempura Udon",
            enDescription = "Udon noodle soup with crispy tempura.",
            tags = listOf("Noodles"),
            images = emptyList()
        )
    )
)

@Composable
private fun SakuraBackgroundDecoration() {
    val launchOffsets = remember {
        val random = Random(System.currentTimeMillis())
        List(4) {
            DpOffset(
                x = random.nextInt(from = -12, until = 13).dp,
                y = random.nextInt(from = -14, until = 15).dp
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 18.dp)
                .offset(
                    x = launchOffsets[0].x,
                    y = 34.dp + launchOffsets[0].y
                ),
            size = 68.dp,
            alpha = 0.16f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 64.dp)
                .offset(
                    x = launchOffsets[1].x,
                    y = launchOffsets[1].y
                ),
            size = 80.dp,
            alpha = 0.14f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 10.dp, top = 122.dp)
                .offset(
                    x = launchOffsets[2].x,
                    y = launchOffsets[2].y
                ),
            size = 52.dp,
            alpha = 0.12f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 148.dp)
                .offset(
                    x = launchOffsets[3].x,
                    y = launchOffsets[3].y
                ),
            size = 60.dp,
            alpha = 0.1f
        )
    }
}

@Composable
private fun SakuraCluster(
    modifier: Modifier,
    size: androidx.compose.ui.unit.Dp,
    alpha: Float
) {
    val petalColor = Color(0xFFF3A8BE).copy(alpha = alpha)
    val centerColor = Color(0xFFF9D6E1).copy(alpha = alpha + 0.05f)
    val petalSize = size * 0.34f

    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.TopCenter)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.CenterStart)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.CenterEnd)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.BottomStart)
                .offset(x = 10.dp)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.BottomEnd)
                .offset(x = (-10).dp)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(size * 0.22f)
                .align(Alignment.Center)
                .background(centerColor, CircleShape)
        )
    }
}

