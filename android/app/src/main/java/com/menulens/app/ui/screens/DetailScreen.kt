package com.menulens.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.menulens.app.BuildConfig
import com.menulens.app.model.MenuItem
import com.menulens.app.model.MenuPreview
import com.menulens.app.viewmodel.ResultsUiState

@Composable
fun DetailScreen(
    state: ResultsUiState,
    itemId: String,
    onReveal: () -> Unit,
    onShowToStaff: () -> Unit
) {
    val realItem = state.itemById(itemId)
    val debugFallbackItem = remember(itemId) { debugDetailItemById(itemId) }
    val item = realItem ?: if (BuildConfig.DEBUG) debugFallbackItem else null
    val unlocked = state.isUnlocked(itemId) || (BuildConfig.DEBUG && item != null)
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }

    AppScreen(
        title = "",
        subtitle = "",
        showHeaderCard = false
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            DetailSakuraDecoration()

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Item Detail",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier.border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(24.dp)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        if (unlocked && item != null) {
                            Text(
                                text = item.preview.enTitle,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                                Text(
                                    text = item.preview.enDescription,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = item.jpText,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                            )
                            DishImageSection(
                                imageUrls = item.preview.images,
                                onImageTap = { expandedImageUrl = it }
                            )
                            Box(modifier = Modifier.padding(horizontal = 6.dp)) {
                                Text(
                                    text = "Tap an image to expand. If none appear, image search may be unavailable.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                                )
                            }
                        } else {
                            Text(
                                text = "Locked content",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            LockedPlaceholder()
                            LockedPlaceholder()
                            Button(
                                onClick = onReveal,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    if (state.creditsRemainingToday > 0 || state.isPro) {
                                        "Reveal (${state.creditsRemainingToday} left today)"
                                    } else {
                                        "Upgrade to unlock"
                                    }
                                )
                            }
                        }
                    }
                }

                if (unlocked && item != null) {
                    ShowToStaffBlock(
                        onShowToStaff = onShowToStaff,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    }

    expandedImageUrl?.let { imageUrl ->
        ExpandedImageDialog(
            imageUrl = imageUrl,
            onDismiss = { expandedImageUrl = null }
        )
    }
}

@Composable
private fun DetailSakuraDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 42.dp, end = 16.dp),
            size = 54.dp,
            alpha = 0.16f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 68.dp),
            size = 74.dp,
            alpha = 0.14f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 26.dp, bottom = 220.dp),
            size = 58.dp,
            alpha = 0.15f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 12.dp)
                .offset(y = 150.dp),
            size = 52.dp,
            alpha = 0.14f
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

@Composable
private fun LockedPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(20.dp)
            .background(
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                shape = RoundedCornerShape(10.dp)
            )
    )
}

@Composable
private fun DishImageSection(
    imageUrls: List<String>,
    onImageTap: (String) -> Unit
) {
    val validUrls = imageUrls.filter { it.isNotBlank() }
    if (validUrls.isEmpty()) {
        FoodImageFallback(message = "Images unavailable for this item.")
        return
    }

    RemoteDishImageCard(
        imageUrls = validUrls,
        onClick = onImageTap
    )
}

@Composable
private fun RemoteDishImageCard(
    imageUrls: List<String>,
    onClick: (String) -> Unit
) {
    var activeIndex by remember(imageUrls) { mutableStateOf(0) }
    val activeUrl = imageUrls.getOrNull(activeIndex)
    val hasMoreCandidates = activeIndex < imageUrls.lastIndex

    if (activeUrl == null) {
        FoodImageFallback(message = "Image failed to load.")
        return
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable(onClick = { onClick(activeUrl) })
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        SubcomposeAsyncImage(
            model = activeUrl,
            contentDescription = "Dish image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
            loading = {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            error = {
                if (hasMoreCandidates) {
                    activeIndex += 1
                } else {
                    FoodImageFallback(message = "Image failed to load.")
                }
            }
        )
    }
}

@Composable
private fun ShowToStaffBlock(
    onShowToStaff: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onShowToStaff,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.tertiary,
            contentColor = MaterialTheme.colorScheme.onTertiary
        )
    ) {
        Text(
            text = "Show to staff",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun FoodImageFallback(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
                shape = RoundedCornerShape(14.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(10.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}

private fun debugDetailItemById(itemId: String): MenuItem? {
    val debugItems = mapOf(
        "debug_1" to MenuItem(
            itemId = "debug_1",
            jpText = "いなり寿司",
            priceText = "350 JPY",
            preview = MenuPreview(
                enTitle = "Inari Sushi",
                enDescription = "Sweet and savory fried tofu pouches filled with seasoned sushi rice.",
                tags = listOf("Sushi"),
                images = listOf("https://images.unsplash.com/photo-1617196034796-73dfa7b1fd56")
            )
        ),
        "debug_2" to MenuItem(
            itemId = "debug_2",
            jpText = "天ぷらうどん",
            priceText = "980 JPY",
            preview = MenuPreview(
                enTitle = "Tempura Udon",
                enDescription = "Udon noodle soup served with crispy tempura and light dashi broth.",
                tags = listOf("Noodles"),
                images = listOf("https://images.unsplash.com/photo-1618841557871-b4664fbf0cb3")
            )
        )
    )
    return debugItems[itemId]
}

@Composable
private fun ExpandedImageDialog(
    imageUrl: String,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp)
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = "Expanded dish image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                },
                error = {
                    FoodImageFallback(message = "Image failed to load.")
                }
            )
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Close")
            }
        }
    }
}

