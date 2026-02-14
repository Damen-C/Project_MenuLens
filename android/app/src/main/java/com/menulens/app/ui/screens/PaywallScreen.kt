package com.menulens.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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

@Composable
fun PaywallScreen(
    isPro: Boolean,
    onEnablePro: () -> Unit,
    onDisablePro: () -> Unit,
    onRestore: () -> Unit
) {
    AppScreen(
        title = "Upgrade to Pro",
        subtitle = "Cute outside, strict rules inside: free list, paid unlimited detail reveals."
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
            modifier = Modifier.border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f),
                shape = RoundedCornerShape(24.dp)
            )
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Unlimited detail reveals",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "List view remains free. English title, description, tags, and images are gated.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.85f)
                )
                Button(
                    onClick = if (isPro) onDisablePro else onEnablePro,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text(if (isPro) "Disable Pro (Debug)" else "Enable Pro (Debug)")
                }
                Button(
                    onClick = onRestore,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary, contentColor = MaterialTheme.colorScheme.onTertiary)
                ) {
                    Text("Restore (Stub)")
                }
            }
        }
    }
}
