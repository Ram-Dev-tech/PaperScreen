package com.paperscreen.android.paper.engine

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.paperscreen.android.R

/**
 * A shared Live Preview component to demonstrate how the Paper Rendering Engine affects content.
 * Built-in apps can use this to quickly test different engine configs.
 */
@Composable
fun PaperPreview(config: PaperRenderConfig, modifier: Modifier = Modifier) {
    PaperEnvironmentProvider(config = config, modifier = modifier) {
        // Inner Content representing a standard colorful Android UI
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Live Preview", style = MaterialTheme.typography.titleMedium, color = Color.Black)
            
            // Colorful gradient to test thresholds
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Blue, Color.Red, Color.Yellow)
                        )
                    )
            )

            // Text contrast testing
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Sample UI Element", style = MaterialTheme.typography.bodyMedium, color = Color.DarkGray)
                    Text("Testing light vs dark mapping", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
                
                // Pure colored shapes to test pure black/white
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black))
                    Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.White))
                }
            }
        }
    }
}
