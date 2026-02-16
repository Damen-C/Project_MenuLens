package com.menulens.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.SubcomposeAsyncImage
import com.menulens.app.viewmodel.ResultsUiState

@Composable
fun DetailScreen(
    state: ResultsUiState,
    itemId: String,
    onReveal: () -> Unit,
    onShowToStaff: () -> Unit
) {
    val item = state.itemById(itemId)
    val unlocked = state.isUnlocked(itemId)
    var expandedImageUrl by remember { mutableStateOf<String?>(null) }

    AppScreen(
        title = "Item Detail",
        subtitle = ""
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (unlocked && item != null) {
                        Text(
                            text = item.preview.enTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.preview.enDescription,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = item.jpText,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                        )
                        DishImageSection(
                            imageUrls = item.preview.images,
                            onImageTap = { expandedImageUrl = it }
                        )
                        Text(
                            text = "Tap an image to expand. If none appear, image search may be unavailable.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
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
                ShowToStaffBlock(onShowToStaff = onShowToStaff)
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
    onShowToStaff: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Button(
            onClick = onShowToStaff,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        ) {
            Text("Show to staff")
        }
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
