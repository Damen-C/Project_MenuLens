package com.menulens.app.data

import com.menulens.app.model.MenuItem
import com.menulens.app.model.MenuPreview
import com.squareup.moshi.Json

data class ScanMenuResponseDto(
    @Json(name = "scan_id") val scanId: String,
    val items: List<ScanItemDto>
)

data class ScanItemDto(
    @Json(name = "item_id") val itemId: String,
    @Json(name = "jp_text") val jpText: String,
    @Json(name = "price_text") val priceText: String?,
    val preview: PreviewDto
)

data class PreviewDto(
    @Json(name = "en_title") val enTitle: String,
    @Json(name = "en_description") val enDescription: String,
    val tags: List<String>,
    val images: List<ImagePreviewDto>
)

data class ImagePreviewDto(
    val url: String
)

fun ScanMenuResponseDto.toMenuItems(): List<MenuItem> {
    return items.map { item ->
        MenuItem(
            itemId = item.itemId,
            jpText = item.jpText,
            priceText = item.priceText,
            preview = MenuPreview(
                enTitle = item.preview.enTitle,
                enDescription = item.preview.enDescription,
                tags = item.preview.tags,
                images = item.preview.images.map { it.url }
            )
        )
    }
}
