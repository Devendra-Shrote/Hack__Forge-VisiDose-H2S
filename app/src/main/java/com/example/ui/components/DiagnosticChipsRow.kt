package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyanBadgeBg
import com.example.ui.theme.DeepCyanText
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.ErrorRedBg
import com.example.ui.theme.ErrorRedText
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.WarningOrangeBg
import com.example.ui.theme.WarningOrangeText

@Composable
fun DiagnosticChipsRow(
    fiducialsDetected: Boolean,
    lightingOk: Boolean,
    onToggleFiducials: () -> Unit = {},
    onToggleLighting: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Reference Detected Chip
        val refBg = if (fiducialsDetected) MintBadgeBg else ErrorRedBg
        val refFg = if (fiducialsDetected) EmeraldText else ErrorRedText
        val refText = if (fiducialsDetected) "Reference detected ✓" else "Target Not Found"
        val refIcon = if (fiducialsDetected) Icons.Default.Check else Icons.Default.Close

        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(refBg)
                .clickable { onToggleFiducials() }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = refIcon,
                    contentDescription = null,
                    tint = refFg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = refText,
                    color = refFg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Lighting Status Chip
        val lightBg = if (lightingOk) CyanBadgeBg else WarningOrangeBg
        val lightFg = if (lightingOk) DeepCyanText else WarningOrangeText
        val lightText = if (lightingOk) "Lighting OK ✓" else "Glare / Low Light"
        val lightIcon = if (lightingOk) Icons.Default.LightMode else Icons.Default.WarningAmber

        Box(
            modifier = Modifier
                .weight(1f)
                .height(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(lightBg)
                .clickable { onToggleLighting() }
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = lightIcon,
                    contentDescription = null,
                    tint = lightFg,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = lightText,
                    color = lightFg,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
