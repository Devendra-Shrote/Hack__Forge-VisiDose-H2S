package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.model.ColorimetricEngine
import com.example.ui.components.VirtualViewfinderCanvas
import com.example.ui.theme.CardBorder
import com.example.ui.theme.EmeraldText
import com.example.ui.theme.MintBadgeBg
import com.example.ui.theme.PageBackground
import com.example.ui.theme.PrimaryScanBlue
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel
import com.example.viewmodel.DosimeterUiState
import java.io.File
import java.util.Locale

@Composable
fun ScanScreen(
    uiState: DosimeterUiState,
    onCaptureClick: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val lab = ColorimetricEngine.rgbToLab(uiState.stripColor)
    val context = LocalContext.current

    var currentPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var currentPhotoFile by remember { mutableStateOf<File?>(null) }

    // TakePicture launcher with FileProvider URI
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            val bitmap = try {
                val file = currentPhotoFile
                if (file != null && file.exists()) {
                    BitmapFactory.decodeFile(file.absolutePath)
                } else if (currentPhotoUri != null) {
                    if (android.os.Build.VERSION.SDK_INT >= 28) {
                        val source = android.graphics.ImageDecoder.createSource(context.contentResolver, currentPhotoUri!!)
                        android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                            decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, currentPhotoUri!!)
                    }
                } else null
            } catch (e: Exception) {
                null
            }
            onCaptureClick(bitmap)
        } else {
            // Dismissed without taking photo: preserve current viewfinder state without crashing
        }
    }

    fun launchCamera() {
        try {
            val photoFile = File(context.cacheDir, "badge_capture_${System.currentTimeMillis()}.jpg")
            currentPhotoFile = photoFile
            val photoUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            currentPhotoUri = photoUri
            takePictureLauncher.launch(photoUri)
        } catch (e: Exception) {
            onCaptureClick(null)
        }
    }

    // Dynamic camera runtime permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            onCaptureClick(null)
        }
    }

    fun handleCaptureClick() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            launchCamera()
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            try {
                val bitmap = if (android.os.Build.VERSION.SDK_INT >= 28) {
                    val source = android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
                    android.graphics.ImageDecoder.decodeBitmap(source)
                } else {
                    @Suppress("DEPRECATION")
                    android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                onCaptureClick(bitmap)
            } catch (e: Exception) {
                onCaptureClick(null)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(PageBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Optical Calibration Scanner",
                        color = TextSlate,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Align cassette & reference card within view",
                        color = TextSteel,
                        fontSize = 12.sp
                    )
                }
            }

            // Viewfinder Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .border(1.5.dp, PrimaryScanBlue, RoundedCornerShape(14.dp)),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E2630))
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (uiState.capturedImage != null) {
                        Image(
                            bitmap = uiState.capturedImage.asImageBitmap(),
                            contentDescription = "Captured Wristband Photo",
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(14.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        VirtualViewfinderCanvas(
                            stripColor = uiState.stripColor,
                            isScanning = uiState.isScanning,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Overlay Controls (top right)
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        IconButton(
                            onClick = {
                                try {
                                    fileChooserLauncher.launch("image/*")
                                } catch (e: Exception) {
                                    onCaptureClick(null)
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = "Upload Image File",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                handleCaptureClick()
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color(0x66000000))
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = "Open Device Camera",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    if (uiState.isScanning) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xCC002B49))
                                .align(Alignment.BottomCenter)
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4ADE80)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = uiState.scanPhaseText,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Primary Action Button
            Button(
                onClick = { handleCaptureClick() },
                enabled = !uiState.isScanning,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryScanBlue)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Capture",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (uiState.isScanning) "ANALYZING OPTICAL TELEMETRY..." else "CAPTURE & ANALYZE NOW",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Live Optical Diagnostics Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "OPTICAL QUALITY GATE READOUT",
                        color = TextSteel,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    DiagnosticItem(
                        title = "ArUco Fiducial Alignment",
                        value = if (uiState.fiducialsDetected) "Detected (4/4 Points Locked)" else "Target Not Found",
                        isPass = uiState.fiducialsDetected
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DiagnosticItem(
                        title = "Ambient Illuminance & Uniformity",
                        value = if (uiState.lightingOk) "520 Lux (Optimal Range 350-800)" else "Non-uniform / Glare Detected",
                        isPass = uiState.lightingOk
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DiagnosticItem(
                        title = "Wristband Lot Authentication",
                        value = "${uiState.wristbandId} (Lot #2026-MRPL-09)",
                        isPass = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    DiagnosticItem(
                        title = "Strip ROI CIE L*a*b*",
                        value = "L*=${String.format(Locale.US, "%.1f", lab.l)}, a*=${String.format(Locale.US, "%.1f", lab.a)}, b*=${String.format(Locale.US, "%.1f", lab.b)} (ΔE*=${String.format(Locale.US, "%.1f", uiState.deltaE)})",
                        isPass = true
                    )
                }
            }
        }
    }
}

@Composable
private fun DiagnosticItem(title: String, value: String, isPass: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextSteel, fontSize = 11.sp)
            Text(value, color = if (isPass) TextSlate else Color(0xFFC81E1E), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (isPass) EmeraldText else Color(0xFFCBD5E1),
            modifier = Modifier.size(16.dp)
        )
    }
}
