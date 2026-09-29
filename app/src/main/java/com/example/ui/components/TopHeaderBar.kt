package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BrandHeaderBlue
import com.example.ui.theme.BrandHeaderDark
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TopHeaderBar(
    onOpenSettings: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(BrandHeaderDark, BrandHeaderBlue)
                )
            )
            .statusBarsPadding()
    ) {
        // App Title Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cu-PAN H2S Badge Logo
            H2sCircularLogo()

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "VisiDose-H2S",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "H2S Exposure Dosimeter",
                    color = Color(0xFFD4E6F7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(38.dp)
                    .testTag("settings_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
fun H2sCircularLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(Color(0xFF00385E)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(40.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val outerRadius = size.width / 2f - 2f
            val innerRadius = outerRadius - 4f

            // Outer sunburst rays
            for (i in 0 until 16) {
                val angle = (i * 360f / 16f) * (Math.PI / 180f).toFloat()
                val r1 = if (i % 2 == 0) outerRadius - 1f else outerRadius - 4f
                val r0 = outerRadius - 6f
                val p0 = Offset(center.x + r0 * cos(angle), center.y + r0 * sin(angle))
                val p1 = Offset(center.x + r1 * cos(angle), center.y + r1 * sin(angle))
                drawLine(
                    color = Color(0xFFFFD54F),
                    start = p0,
                    end = p1,
                    strokeWidth = 1.5f
                )
            }

            // Inner circle ring
            drawCircle(
                color = Color(0xFFFFD54F),
                radius = innerRadius - 3f,
                style = Stroke(width = 1.2f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "H₂S",
                color = Color(0xFFFFE082),
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-0.5).sp
            )
        }
    }
}
