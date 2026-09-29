package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.CardBorder
import com.example.ui.theme.ErrorRedBg
import com.example.ui.theme.ErrorRedText
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryScanBlue
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel
import com.example.ui.theme.WarningOrangeBg
import com.example.ui.theme.WarningOrangeText

@Composable
fun AlertsScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Alerts",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Safety & Threshold Alerts",
                        color = TextSlate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Real-time industrial hygiene monitor",
                        color = TextSteel,
                        fontSize = 11.sp
                    )
                }
            }

            // Emergency Quick Contact Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MRPL Emergency Control Room",
                            color = ErrorRedText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Hotline: +91 824 2270400 | Channel 4 UHF",
                            color = Color(0xFF991B1B),
                            fontSize = 11.sp
                        )
                    }
                    Button(
                        onClick = { /* simulated emergency call */ },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRedText),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhoneInTalk,
                            contentDescription = "Call",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SOS", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // List of Notifications / Alerts
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    AlertCard(
                        title = "Shift Cumulative Dose: 28.5 ppm-h (CST)",
                        time = "Today, 09:41 AM",
                        description = "Calculated Cu-PAN cumulative dose is at 28.5 ppm-h. OSHA Permissible Exposure Limit (PEL) is 20 ppm ceiling, ACGIH TLV-TWA is 1 ppm 8-hr. Continue mandatory ventilation & monitor active detector.",
                        severity = "WARNING"
                    )
                }
                item {
                    AlertCard(
                        title = "Consumable Shelf Life: 2 Days Remaining",
                        time = "Today, 07:00 AM",
                        description = "Wristband #WB-7842-3A has reached 28 days of service life. Please swap with a fresh hermetically sealed Cu-PAN strip from safety dispensary before tomorrow's shift.",
                        severity = "INFO"
                    )
                }
                item {
                    AlertCard(
                        title = "Safety Reminder: H₂S Olfactory Fatigue",
                        time = "Shift Briefing",
                        description = "Hydrogen Sulfide paralyzes olfactory sensory nerves above 100 ppm. NEVER rely on smell of rotten eggs to detect lethal gas pockets in refinery sump areas.",
                        severity = "CRITICAL"
                    )
                }
                item {
                    AlertCard(
                        title = "Perimeter Station Sensor #P-14 Nominal",
                        time = "Yesterday, 11:30 PM",
                        description = "Continuous optical fixed beam monitor at CDU-II perimeter reported normal baseline ambient air quality (0.05 ppm).",
                        severity = "NORMAL"
                    )
                }
            }
        }
    }
}

@Composable
fun AlertCard(
    title: String,
    time: String,
    description: String,
    severity: String
) {
    val (bg, border, iconColor, icon) = when (severity) {
        "CRITICAL" -> Quad(Color(0xFFFEF2F2), Color(0xFFFCA5A5), ErrorRedText, Icons.Default.Warning)
        "WARNING" -> Quad(Color(0xFFFFFBEB), Color(0xFFFDE68A), Color(0xFFD97706), Icons.Default.Warning)
        "INFO" -> Quad(Color(0xFFEFF6FF), Color(0xFFBFDBFE), PrimaryScanBlue, Icons.Default.Info)
        else -> Quad(Color.White, CardBorder, PrimaryScanBlue, Icons.Default.Info)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, border, RoundedCornerShape(10.dp)),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = title,
                        color = TextSlate,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = time,
                        color = TextSteel,
                        fontSize = 10.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    color = Color(0xFF334155),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
