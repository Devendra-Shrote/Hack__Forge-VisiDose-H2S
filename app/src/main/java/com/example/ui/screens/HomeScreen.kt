package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.data.ExposureReadingEntity
import com.example.ui.components.DiagnosticChipsRow
import com.example.ui.components.EnvironmentalTelemetryCard
import com.example.ui.components.ExposureHistoryCard
import com.example.ui.components.ResultsCardsRow
import com.example.ui.components.WorkerShiftCard
import com.example.ui.components.WristbandScanCard
import com.example.ui.theme.PageBackground
import com.example.viewmodel.DosimeterUiState

@Composable
fun HomeScreen(
    uiState: DosimeterUiState,
    historyReadings: List<ExposureReadingEntity>,
    onCaptureClick: (android.graphics.Bitmap?) -> Unit,
    onToggleFiducials: () -> Unit,
    onToggleLighting: () -> Unit,
    onViewAllHistoryClick: () -> Unit,
    onScanBadgeDataClick: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Worker Profile & Shift Header
            WorkerShiftCard(
                workerId = uiState.workerId,
                shiftName = uiState.shiftName,
                dateTimeFormatted = uiState.timestampFormatted
            )

            // 2. Scan Wristband Strip & Virtual Viewfinder
            WristbandScanCard(
                wristbandId = uiState.wristbandId,
                stripColor = uiState.stripColor,
                isScanning = uiState.isScanning,
                scanPhaseText = uiState.scanPhaseText,
                readingState = uiState.readingState,
                capturedImage = uiState.capturedImage,
                onCaptureClick = onCaptureClick
            )

            // 3. Diagnostic Badges: Reference detected ✓ & Lighting OK
            DiagnosticChipsRow(
                fiducialsDetected = uiState.fiducialsDetected,
                lightingOk = uiState.lightingOk,
                onToggleFiducials = onToggleFiducials,
                onToggleLighting = onToggleLighting
            )

            // 4. Core Results Panel: Estimated Exposure & Badge Valid
            ResultsCardsRow(
                readingState = uiState.readingState,
                exposureDisplayValue = uiState.exposureDisplayValue,
                exposureSubtitle = uiState.exposureSubtitle,
                shelfLifeDays = uiState.shelfLifeDays,
                failureReason = uiState.failureReason,
                onScanBadgeDataClick = onScanBadgeDataClick
            )

            // 5. Environmental Telemetry: Temperature, Humidity, Strip Status
            EnvironmentalTelemetryCard(
                temperature = uiState.ambientTemp,
                humidity = uiState.humidity,
                stripStatus = uiState.stripStatusText
            )

            // 6. Exposure History: 7-day cumulative dosage bar chart
            ExposureHistoryCard(
                readings = historyReadings,
                onViewAllClick = onViewAllHistoryClick
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
