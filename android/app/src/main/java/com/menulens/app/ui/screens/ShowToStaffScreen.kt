package com.menulens.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.menulens.app.R

@Composable
fun ShowToStaffScreen(jpText: String, enText: String?, priceText: String?) {
    AppScreen(
        title = "Show to Staff",
        subtitle = "",
        showHeaderCard = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ShowToStaffSakuraDecoration()

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "これをください",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (jpText.isBlank()) "（メニュー項目未選択）" else jpText,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (!priceText.isNullOrBlank()) {
                            Text(
                                text = priceText,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Text(
                            text = "ありがとうございました！",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                        )
                    }
                }

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.94f)
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.38f),
                            shape = RoundedCornerShape(24.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "I'd like this, please.",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (enText.isNullOrBlank()) "No menu item selected" else "Selected item: $enText",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Thank you so much!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.92f)
                        )
                    }
                }

                Image(
                    painter = painterResource(id = R.drawable.macha),
                    contentDescription = "Matcha illustration",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth(0.94f)
                        .height(340.dp)
                )
            }
        }
    }
}

@Composable
private fun ShowToStaffSakuraDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 46.dp, end = 18.dp),
            size = 56.dp,
            alpha = 0.13f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 10.dp)
                .offset(y = 38.dp),
            size = 46.dp,
            alpha = 0.11f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 22.dp, bottom = 96.dp),
            size = 62.dp,
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
                .offset(x = 8.dp)
                .background(petalColor, CircleShape)
        )
        Box(
            modifier = Modifier
                .size(petalSize)
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp)
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
