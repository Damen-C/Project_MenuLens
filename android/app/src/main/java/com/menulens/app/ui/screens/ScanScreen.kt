package com.menulens.app.ui.screens

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import com.menulens.app.R
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect

@Composable
fun ScanScreen(onMenuImageReady: (ByteArray) -> Unit) {
    val context = LocalContext.current
    var permissionMessage by remember { mutableStateOf<String?>(null) }
    var capturedPreview by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            capturedPreview = bitmap
            onMenuImageReady(bitmap.toJpegByteArray())
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            permissionMessage = null
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null && bytes.isNotEmpty()) {
                onMenuImageReady(bytes)
            } else {
                permissionMessage = "Could not read selected image. Please try another photo."
            }
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            permissionMessage = null
            cameraLauncher.launch(null)
        } else {
            permissionMessage = "Camera permission is required to scan Japanese menus."
        }
    }

    AppScreen(
        title = "",
        subtitle = "",
        centerContent = true,
        showBrandAsBlock = false,
        showHeaderCard = false,
        topPadding = 30.dp
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScanSakuraDecoration()

            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Column(
                    modifier = Modifier.offset(y = (-18).dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "MenuLens",
                        style = MaterialTheme.typography.displayMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Understand Japanese menus",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Take a photo or upload a menu image to get English dish explanations.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                    )
                }
                JapaneseFoodCarousel()
                Card(
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.45f),
                            shape = RoundedCornerShape(28.dp)
                        )
                ) {
                    Column(
                        modifier = Modifier.padding(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Ready to scan a Japanese menu",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = "For best accuracy, scan a smaller section instead of too many items at once.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                        capturedPreview?.let { bitmap ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Image(
                                    bitmap = bitmap.asImageBitmap(),
                                    contentDescription = "Captured menu preview",
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        permissionMessage?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Button(
                            onClick = {
                                val hasCameraPermission = ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) == PackageManager.PERMISSION_GRANTED
                                if (hasCameraPermission) {
                                    permissionMessage = null
                                    cameraLauncher.launch(null)
                                } else {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text(
                                text = "Scan Menu",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Button(
                            onClick = { galleryLauncher.launch("image/*") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text(
                                text = "Upload Existing Menu Photo",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ScanSakuraDecoration() {
    Box(modifier = Modifier.fillMaxSize()) {
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 24.dp, bottom = 84.dp),
            size = 62.dp,
            alpha = 0.11f
        )
        SakuraCluster(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 126.dp)
                .offset(x = 72.dp),
            size = 50.dp,
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

private data class CarouselFoodItem(
    val title: String,
    val drawableRes: Int
)

private val carouselFoodItems = listOf(
    CarouselFoodItem(
        title = "Sushi",
        drawableRes = R.drawable.sushi
    ),
    CarouselFoodItem(
        title = "Ramen",
        drawableRes = R.drawable.ramen
    ),
    CarouselFoodItem(
        title = "Tempura",
        drawableRes = R.drawable.tempura
    ),
    CarouselFoodItem(
        title = "Kushi",
        drawableRes = R.drawable.kushi
    ),
)

@Composable
private fun JapaneseFoodCarousel() {
    val loopItems = remember { carouselFoodItems + carouselFoodItems + carouselFoodItems }
    val startOffset = 2
    val listState = rememberLazyListState(
        initialFirstVisibleItemIndex = carouselFoodItems.size + startOffset
    )

    LaunchedEffect(Unit) {
        while (true) {
            delay(2600)
            val current = listState.firstVisibleItemIndex
            val next = current + 1
            listState.animateScrollToItem(next)

            if (next >= (carouselFoodItems.size * 2) + startOffset) {
                listState.scrollToItem(carouselFoodItems.size + startOffset)
            }
        }
    }

    LazyRow(
        state = listState,
        userScrollEnabled = false,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(loopItems) { item ->
            FoodCarouselCard(item = item)
        }
    }
}

@Composable
private fun FoodCarouselCard(item: CarouselFoodItem) {
    Image(
        painter = painterResource(id = item.drawableRes),
        contentDescription = item.title,
        contentScale = ContentScale.Fit,
        modifier = Modifier
            .width(120.dp)
            .height(90.dp)
            .clip(RoundedCornerShape(16.dp))
    )
}

private fun Bitmap.toJpegByteArray(quality: Int = 90): ByteArray {
    val stream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.JPEG, quality, stream)
    return stream.toByteArray()
}

