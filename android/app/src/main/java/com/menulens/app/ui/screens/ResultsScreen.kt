package com.menulens.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.menulens.app.viewmodel.ResultsUiState

@Composable
fun ResultsScreen(
    state: ResultsUiState,
    onItemTap: (String) -> Unit,
    onShowToStaff: (String) -> Unit
) {
    AppScreen(
        title = "Scan Results",
        subtitle = "Japanese name and price are always visible. Reveal details for English guidance."
    ) {
        Text(
            text = if (state.isPro) {
                "Pro mode active: unlimited reveals"
            } else {
                "Free reveals left today (Tokyo): ${state.creditsRemainingToday}"
            },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            state.items.forEach { item ->
                val unlocked = state.isUnlocked(item.itemId)
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 2.dp,
                            color = if (unlocked) {
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
                            },
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onItemTap(item.itemId) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.jpText,
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = item.priceText.orEmpty(),
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Text(
                            text = if (unlocked) "Unlocked: ${item.preview.enTitle}" else "Locked • Tap to reveal details",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f)
                        )

                        Button(
                            onClick = { onShowToStaff(item.itemId) },
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
            }
        }
    }
}

