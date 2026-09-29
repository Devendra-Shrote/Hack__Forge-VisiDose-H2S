package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.BottomNavBar
import com.example.ui.components.TopHeaderBar
import com.example.ui.screens.AlertsScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ScanScreen
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryScanBlue
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel
import com.example.viewmodel.DosimeterViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: DosimeterViewModel
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val historyReadings by viewModel.historicalReadings.collectAsStateWithLifecycle()
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Authentication Gate as initial route
    if (!uiState.isAuthenticated) {
        LoginScreen(
            initialWorkerId = uiState.workerId,
            initialPin = "1042-SEC",
            onLoginSuccess = { authorizedWorkerId ->
                viewModel.authenticate(authorizedWorkerId)
            }
        )
        return
    }

    Scaffold(
        topBar = {
            TopHeaderBar(
                onOpenSettings = { showSettingsDialog = true }
            )
        },
        bottomBar = {
            BottomNavBar(
                selectedTab = uiState.currentTab,
                onTabSelected = { viewModel.setTab(it) },
                alertCount = uiState.alertCount
            )
        },
        containerColor = PageBackground
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.currentTab) {
                0 -> HomeScreen(
                    uiState = uiState,
                    historyReadings = historyReadings,
                    onCaptureClick = { bitmap -> viewModel.triggerCaptureAndAnalyze(bitmap) },
                    onToggleFiducials = { viewModel.toggleFiducials() },
                    onToggleLighting = { viewModel.toggleLightingOk() },
                    onViewAllHistoryClick = { viewModel.setTab(1) },
                    onScanBadgeDataClick = { viewModel.setTab(2) }
                )
                1 -> HistoryScreen(
                    readings = historyReadings,
                    onBack = { viewModel.setTab(0) }
                )
                2 -> ScanScreen(
                    uiState = uiState,
                    onCaptureClick = { bitmap -> viewModel.triggerCaptureAndAnalyze(bitmap) }
                )
                3 -> ProfileScreen(
                    uiState = uiState,
                    onSignOut = { viewModel.signOut() }
                )
                4 -> AlertsScreen()
            }
        }
    }

    // Settings & Technical Specifications Dialog
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text(
                    text = "VisiDose-H2S Specifications",
                    color = TextSlate,
                    fontSize = 17.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Deployment: Mangalore Refinery and Petrochemicals Ltd. (MRPL)",
                        color = PrimaryScanBlue,
                        fontSize = 12.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Active Operator: ${uiState.workerId} (${uiState.shiftName})",
                        color = TextSlate,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Sensor Mechanism: Copper(II)-1-(2-pyridylazo)-2-naphthol (Cu-PAN) chelate undergoes selective reaction with atmospheric H2S forming CuS precipitant, producing a quantitative spectral shift from white -> yellow -> orange -> brown -> plum.",
                        color = TextSteel,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Operating Envelope: 15°C to 45°C, 20% to 85% RH. Saturated lockout threshold at 200 ppm-h.",
                        color = TextSteel,
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryScanBlue)
                ) {
                    Text("Close")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showSettingsDialog = false
                        viewModel.signOut()
                    }
                ) {
                    Text("Sign Out")
                }
            }
        )
    }
}
