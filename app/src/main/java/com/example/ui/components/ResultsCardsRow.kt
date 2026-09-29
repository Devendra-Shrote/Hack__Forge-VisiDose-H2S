package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.model.ReadingState
import com.example.ui.theme.AmberExposureBg
import com.example.ui.theme.AmberExposureBorder
import com.example.ui.theme.AmberPillBg
import com.example.ui.theme.DeepAmberText
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.ErrorRedBg
import com.example.ui.theme.ErrorRedBorder
import com.example.ui.theme.ErrorRedText
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel

@Composable
fun ResultsCardsRow(
    readingState: ReadingState,
    exposureDisplayValue: String,
    exposureSubtitle: String,
    shelfLifeDays: Int,
    failureReason: String = "",
    onScanBadgeDataClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Left Card: Estimated Exposure
        EstimatedExposureCard(
            readingState = readingState,
            exposureDisplayValue = exposureDisplayValue,
            exposureSubtitle = exposureSubtitle,
            failureReason = failureReason,
            modifier = Modifier.weight(1f)
        )

        // Right Card: Badge Validity & Shelf Life
        BadgeValidityCard(
            shelfLifeDays = shelfLifeDays,
            readingState = readingState,
            onScanBadgeDataClick = onScanBadgeDataClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun EstimatedExposureCard(
    readingState: ReadingState,
    exposureDisplayValue: String,
    exposureSubtitle: String,
    failureReason: String,
    modifier: Modifier = Modifier
) {
    val isWithheld = readingState == ReadingState.OUT_OF_RANGE || readingState == ReadingState.IMAGE_INVALID
    val cardBg = if (isWithheld) ErrorRedBg else AmberExposureBg
    val cardBorder = if (isWithheld) ErrorRedBorder else AmberExposureBorder
    val accentColor = if (isWithheld) ErrorRedText else DeepAmberText

    Card(
        modifier = modifier
            .height(138.dp)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Science Flask Icon + Title
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (isWithheld) Icons.Default.Warning else Icons.Default.Science,
                    contentDescription = "Flask",
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Estimated Exposure",
                    color = TextSteel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Value Display
            if (isWithheld) {
                Text(
                    text = "Estimate withheld",
                    color = ErrorRedText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 18.sp
                )
                Text(
                    text = "manual review required",
                    color = ErrorRedText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            } else if (readingState == ReadingState.EXPIRED_LOCKOUT) {
                Text(
                    text = "LOCKED",
                    color = ErrorRedText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Strip Expired",
                    color = ErrorRedText,
                    fontSize = 11.sp
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    val parts = exposureDisplayValue.split(" ")
                    val num = parts.getOrNull(0) ?: exposureDisplayValue
                    val unit = parts.getOrNull(1) ?: "ppm-h"

                    Text(
                        text = num,
                        color = TextSlate,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$unit $exposureSubtitle",
                        color = TextSlate,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // CSTIMATE Pill Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isWithheld) Color(0xFFFDE2E2) else AmberPillBg)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
                Text(
                    text = if (isWithheld) "WITHHELD" else "CSTIMATE",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

@Composable
fun BadgeValidityCard(
    shelfLifeDays: Int,
    readingState: ReadingState,
    onScanBadgeDataClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExpired = shelfLifeDays <= 0 || readingState == ReadingState.EXPIRED_LOCKOUT
    val cardBg = if (isExpired) ErrorRedBg else Color.White
    val cardBorder = if (isExpired) ErrorRedBorder else Color(0xFFD1F2E2)
    val checkColor = if (isExpired) ErrorRedText else EmeraldText

    Card(
        modifier = modifier
            .height(138.dp)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Checkmark in circle + "Badge Valid"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(if (isExpired) ErrorRedText else EmeraldText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isExpired) Icons.Default.Close else Icons.Default.Check,
                        contentDescription = "Badge Status",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isExpired) "Badge Expired" else "Badge Valid ✓",
                    color = checkColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Shelf Life
            Column {
                Text(
                    text = "Shelf Life",
                    color = TextSteel,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(1.dp))
                Text(
                    text = "$shelfLifeDays / 30 days",
                    color = if (isExpired) ErrorRedText else TextSlate,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // SCAN BADGE FOR DATA Pill Button
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFFE8F8F0))
                    .clickable { onScanBadgeDataClick() }
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = if (isExpired) "EXPIRED LOCKOUT" else "SCAN BADGE FOR DATA",
                    color = if (isExpired) ErrorRedText else EmeraldText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.4.sp
                )
            }
        }
    }
}
