package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExposureReadingEntity
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PaleSkyBlueBorder
import com.example.ui.theme.PaleSkyBlueTint
import com.example.ui.theme.PrimaryScanBlue
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel
import java.util.Locale

@Composable
fun ExposureHistoryCard(
    readings: List<ExposureReadingEntity>,
    onViewAllClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedBarIndex by remember { mutableStateOf<Int?>(null) }

    // Fallback seed data if readings is empty
    val chartData = if (readings.isNotEmpty()) {
        readings.takeLast(7).map { it.dateLabel to it.exposurePpmH.toFloat() }
    } else {
        listOf(
            "15 Sep" to 8.2f,
            "16 Sep" to 14.5f,
            "17 Sep" to 15.8f,
            "18 Sep" to 17.2f,
            "19 Sep" to 19.1f,
            "20 Sep" to 22.4f,
            "21 Sep" to 28.5f
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Clock Icon + "Exposure History" + "View All ->"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "History",
                        tint = PrimaryScanBlue,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Exposure History",
                        color = TextSlate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "View All →",
                    color = PrimaryScanBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { onViewAllClick() }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Selected point banner if user tapped a bar
            selectedBarIndex?.let { idx ->
                val point = chartData.getOrNull(idx)
                if (point != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "${point.first}: ${String.format(Locale.US, "%.1f", point.second)} ppm-h cumulative shift dose",
                            color = Color(0xFF92400E),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Interactive Bar Chart Canvas
            val maxDose = 30f
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                // Y-Axis Labels
                Column(
                    modifier = Modifier
                        .height(115.dp)
                        .padding(end = 6.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    Text("ppm-h", fontSize = 9.sp, color = TextSteel, fontWeight = FontWeight.Bold)
                    Text("30", fontSize = 9.sp, color = TextSteel)
                    Text("20", fontSize = 9.sp, color = TextSteel)
                    Text("10", fontSize = 9.sp, color = TextSteel)
                    Text("0", fontSize = 9.sp, color = TextSteel)
                }

                // Chart Canvas & X-Axis labels
                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(105.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(105.dp)
                                .pointerInput(chartData) {
                                    detectTapGestures { offset ->
                                        val barAreaWidth = size.width
                                        val barSlotWidth = barAreaWidth / chartData.size
                                        val index = (offset.x / barSlotWidth).toInt().coerceIn(0, chartData.size - 1)
                                        selectedBarIndex = index
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height

                            // Draw horizontal light grid lines
                            val gridLevels = listOf(0f, 0.333f, 0.666f, 1f)
                            gridLevels.forEach { lvl ->
                                val y = h * lvl
                                drawLine(
                                    color = Color(0xFFEDF2F7),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1f
                                )
                            }

                            val count = chartData.size
                            val slotWidth = w / count
                            val barWidth = slotWidth * 0.52f

                            for (i in 0 until count) {
                                val value = chartData[i].second.coerceIn(0f, maxDose)
                                val barHeight = (value / maxDose) * h
                                val barX = i * slotWidth + (slotWidth - barWidth) / 2f
                                val barY = h - barHeight

                                val isSelected = selectedBarIndex == i
                                val barColor = when {
                                    isSelected -> Color(0xFFD97706)
                                    i == count - 1 -> Color(0xFFD97706) // Current latest shift in darker amber
                                    else -> Color(0xFFF59E0B) // Amber/orange tone
                                }

                                drawRoundRect(
                                    color = barColor,
                                    topLeft = Offset(barX, barY),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(3f, 3f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // X-Axis Date Labels Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        chartData.forEachIndexed { idx, pair ->
                            Text(
                                text = pair.first,
                                fontSize = 10.sp,
                                color = if (selectedBarIndex == idx) PrimaryScanBlue else TextSteel,
                                fontWeight = if (selectedBarIndex == idx || idx == chartData.size - 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footnote Info Tag
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, PaleSkyBlueBorder, RoundedCornerShape(8.dp))
                    .background(PaleSkyBlueTint)
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = PrimaryScanBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Lighting corrected using reference scale.",
                        color = Color(0xFF2B5375),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
