package com.example.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCodeScanner
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import com.example.model.ReadingState
import com.example.ui.theme.CardBorder
import com.example.ui.theme.CuPan0
import com.example.ui.theme.CuPan10
import com.example.ui.theme.CuPan100
import com.example.ui.theme.CuPan200
import com.example.ui.theme.CuPan50
import com.example.ui.theme.PrimaryScanBlue
import com.example.ui.theme.PrimaryScanBlueDark
import com.example.ui.theme.TextSlate
import com.example.ui.theme.TextSteel

@Composable
fun WristbandScanCard(
    wristbandId: String,
    stripColor: Color,
    isScanning: Boolean,
    scanPhaseText: String,
    readingState: ReadingState,
    capturedImage: Bitmap? = null,
    onCaptureClick: (Bitmap?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val shutterFlashAlpha by animateFloatAsState(
        targetValue = if (isScanning) 0.65f else 0f,
        animationSpec = tween(220),
        label = "shutter_flash"
    )

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
            // Load captured image and trigger DosimeterViewModel analysis state progression
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
            // If permission denied, seamlessly trigger optical analysis progression without crashing
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

    // Media/File Picker Fallback Launcher
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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Camera icon + Title & Subtitle
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F1FB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Camera",
                        tint = PrimaryScanBlue,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "Scan Wristband Strip",
                        color = TextSlate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(1.dp))
                    Text(
                        text = "Place the wristband strip in good lighting and capture with camera.",
                        color = TextSteel,
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Realistic Viewfinder / Optical Viewport
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF1E2630))
            ) {
                // Viewfinder canvas preserving wristband cassette and Cu-PAN calibration card
                VirtualViewfinderCanvas(
                    stripColor = stripColor,
                    isScanning = isScanning,
                    capturedImage = capturedImage,
                    modifier = Modifier.fillMaxSize()
                )

                // Shutter flash effect
                if (shutterFlashAlpha > 0.02f) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.White.copy(alpha = shutterFlashAlpha))
                    )
                }

                // Camera & File Fallback Controls Overlay (top-right)
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Universal File Fallback Button
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

                    // Native Camera Trigger Button
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
                            contentDescription = "Open Camera",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Scanning HUD Overlay
                if (isScanning) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0x22000000)),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xCC002B49))
                                .padding(vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(14.dp),
                                    strokeWidth = 2.dp,
                                    color = Color(0xFF4ADE80)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = scanPhaseText,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Wristband ID Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QR code badge
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(6.dp))
                        .background(Color(0xFFF8FAFC)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = "QR Code",
                        tint = TextSlate,
                        modifier = Modifier.size(30.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = "Wristband ID",
                        color = TextSteel,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = wristbandId,
                        color = TextSlate,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // CAPTURE & ANALYZE Button
            val isLockout = readingState == ReadingState.EXPIRED_LOCKOUT
            Button(
                onClick = { handleCaptureClick() },
                enabled = !isScanning && !isLockout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("capture_analyze_button"),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isLockout) Color(0xFF9E9E9E) else PrimaryScanBlue,
                    disabledContainerColor = if (isLockout) Color(0xFFBDBDBD) else PrimaryScanBlueDark
                )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isLockout) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "WRISTBAND EXPIRED - REPLACE STRIP",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else if (isScanning) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ANALYZING OPTICAL TELEMETRY...",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CAPTURE & ANALYZE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VirtualViewfinderCanvas(
    stripColor: Color,
    isScanning: Boolean,
    capturedImage: Bitmap? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "laser_sweep")
    val laserYRatio by infiniteTransition.animateFloat(
        initialValue = 0.1f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_pos"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        if (capturedImage != null) {
            // Draw captured photo background
            val imgBitmap = capturedImage.asImageBitmap()
            drawImage(
                image = imgBitmap,
                dstOffset = IntOffset.Zero,
                dstSize = IntSize(w.toInt(), h.toInt())
            )
            // Optical guide scrim to ensure alignment reference stays clear
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0x33000000), Color(0x55000000))
                ),
                size = size
            )
        } else {
            // 1. Natural Arm background gradient (skin tone)
            drawRect(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFF8D5524),
                        Color(0xFFC68642),
                        Color(0xFFE0AC69),
                        Color(0xFFC68642),
                        Color(0xFF70401C)
                    )
                ),
                topLeft = Offset(0f, 0f),
                size = Size(w * 0.58f, h)
            )

            // Arm hair/skin subtle texture lines
            for (i in 1..8) {
                drawLine(
                    color = Color(0x334A2E12),
                    start = Offset(w * (0.05f + i * 0.06f), h * 0.1f),
                    end = Offset(w * (0.08f + i * 0.06f), h * 0.25f),
                    strokeWidth = 1f
                )
            }
        }

        // 2. Wristband body (Black rugged silicone/polymer cassette)
        val wbLeft = w * 0.12f
        val wbTop = h * 0.10f
        val wbWidth = w * 0.38f
        val wbHeight = h * 0.80f

        // Watch strap extension top and bottom
        drawRoundRect(
            color = Color(0xFF22262B),
            topLeft = Offset(wbLeft + wbWidth * 0.15f, 0f),
            size = Size(wbWidth * 0.70f, h),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Main cassette case
        drawRoundRect(
            color = Color(0xFF181C20),
            topLeft = Offset(wbLeft, wbTop),
            size = Size(wbWidth, wbHeight),
            cornerRadius = CornerRadius(24f, 24f)
        )
        // Bevel highlight
        drawRoundRect(
            color = Color(0xFF384048),
            topLeft = Offset(wbLeft + 2f, wbTop + 2f),
            size = Size(wbWidth - 4f, wbHeight - 4f),
            cornerRadius = CornerRadius(22f, 22f),
            style = Stroke(width = 2f)
        )

        // Cassette Window (Recessed slot for chemical strip)
        val slotLeft = wbLeft + wbWidth * 0.22f
        val slotTop = wbTop + wbHeight * 0.10f
        val slotWidth = wbWidth * 0.56f
        val slotHeight = wbHeight * 0.45f

        drawRoundRect(
            color = Color(0xFF0F1114),
            topLeft = Offset(slotLeft, slotTop),
            size = Size(slotWidth, slotHeight),
            cornerRadius = CornerRadius(12f, 12f)
        )

        // The reactive Cu-PAN strip inside cassette
        val stripLeft = slotLeft + 4f
        val stripTop = slotTop + 4f
        val stripW = slotWidth - 8f
        val stripH = slotHeight - 8f

        drawRoundRect(
            color = stripColor,
            topLeft = Offset(stripLeft, stripTop),
            size = Size(stripW, stripH),
            cornerRadius = CornerRadius(8f, 8f)
        )

        // Porous gas permeable membrane texture
        for (y in 0 until 5) {
            drawLine(
                color = Color(0x22000000),
                start = Offset(stripLeft, stripTop + (y + 1) * (stripH / 6f)),
                end = Offset(stripLeft + stripW, stripTop + (y + 1) * (stripH / 6f)),
                strokeWidth = 1f
            )
        }

        // Wristband QR Code below strip
        val qrLeft = wbLeft + wbWidth * 0.22f
        val qrTop = slotTop + slotHeight + 8f
        val qrSize = wbWidth * 0.56f

        drawRoundRect(
            color = Color.White,
            topLeft = Offset(qrLeft, qrTop),
            size = Size(qrSize, qrSize * 0.7f),
            cornerRadius = CornerRadius(6f, 6f)
        )
        // QR pseudo patterns
        drawRect(
            color = Color.Black,
            topLeft = Offset(qrLeft + 4f, qrTop + 4f),
            size = Size(8f, 8f)
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(qrLeft + qrSize - 12f, qrTop + 4f),
            size = Size(8f, 8f)
        )
        drawRect(
            color = Color.Black,
            topLeft = Offset(qrLeft + 4f, qrTop + (qrSize * 0.7f) - 12f),
            size = Size(8f, 8f)
        )
        // Center dots
        drawRect(
            color = Color.Black,
            topLeft = Offset(qrLeft + qrSize * 0.4f, qrTop + (qrSize * 0.35f)),
            size = Size(10f, 6f)
        )

        // 3. Five-Swatch Cu-PAN Color Reference Card (Calibration Card)
        val cardLeft = w * 0.56f
        val cardTop = h * 0.08f
        val cardWidth = w * 0.38f
        val cardHeight = h * 0.84f

        // Card backing (White plastic calibration card)
        drawRoundRect(
            color = Color(0xFFFBFDFF),
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(8f, 8f)
        )
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(cardLeft, cardTop),
            size = Size(cardWidth, cardHeight),
            cornerRadius = CornerRadius(8f, 8f),
            style = Stroke(width = 1.5f)
        )

        // 5 swatches
        val swatchH = (cardHeight - 24f) / 5f
        val swatches = listOf(
            Triple(CuPan200, "200 ppm-h", "Plum"),
            Triple(CuPan100, "100 ppm-h", "Brown"),
            Triple(CuPan50, "50 ppm-h", "Orange"),
            Triple(CuPan10, "10 ppm-h", "Pale Yellow"),
            Triple(CuPan0, "0 ppm-h", "White")
        )

        for (idx in swatches.indices) {
            val (swatchColor, _, _) = swatches[idx]
            val sTop = cardTop + 12f + idx * swatchH

            // Swatch color square
            drawRoundRect(
                color = swatchColor,
                topLeft = Offset(cardLeft + 8f, sTop + 2f),
                size = Size(cardWidth * 0.32f, swatchH - 4f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            // Border around swatch (especially needed for 0 ppm-h white)
            drawRoundRect(
                color = Color(0xFF94A3B8),
                topLeft = Offset(cardLeft + 8f, sTop + 2f),
                size = Size(cardWidth * 0.32f, swatchH - 4f),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 0.8f)
            )

            // Text labels represented with clean indicator lines
            drawLine(
                color = Color(0xFF334155),
                start = Offset(cardLeft + cardWidth * 0.44f, sTop + swatchH * 0.35f),
                end = Offset(cardLeft + cardWidth * 0.90f, sTop + swatchH * 0.35f),
                strokeWidth = 2.5f
            )
            drawLine(
                color = Color(0xFF64748B),
                start = Offset(cardLeft + cardWidth * 0.44f, sTop + swatchH * 0.65f),
                end = Offset(cardLeft + cardWidth * 0.80f, sTop + swatchH * 0.65f),
                strokeWidth = 1.8f
            )
        }

        // ArUco Corner Fiducial Markers on the Card
        drawArucoMarker(this, Offset(cardLeft + 3f, cardTop + 3f), 12f)
        drawArucoMarker(this, Offset(cardLeft + cardWidth - 15f, cardTop + 3f), 12f)
        drawArucoMarker(this, Offset(cardLeft + 3f, cardTop + cardHeight - 15f), 12f)
        drawArucoMarker(this, Offset(cardLeft + cardWidth - 15f, cardTop + cardHeight - 15f), 12f)

        // 4. Scanning Laser sweep line & ArUco detection boxes
        if (isScanning) {
            val laserY = h * laserYRatio
            // Laser beam line
            drawLine(
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        Color(0x0000E676),
                        Color(0xFF00E676),
                        Color(0xFFB9F6CA),
                        Color(0xFF00E676),
                        Color(0x0000E676)
                    )
                ),
                start = Offset(0f, laserY),
                end = Offset(w, laserY),
                strokeWidth = 3f
            )

            // Laser glow
            drawRect(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x0000E676),
                        Color(0x3300E676),
                        Color(0x0000E676)
                    ),
                    startY = laserY - 14f,
                    endY = laserY + 14f
                ),
                topLeft = Offset(0f, laserY - 14f),
                size = Size(w, 28f)
            )

            // Optical calibration bounding boxes & reticles
            drawRoundRect(
                color = Color(0xFF00E676),
                topLeft = Offset(stripLeft - 4f, stripTop - 4f),
                size = Size(stripW + 8f, stripH + 8f),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 2f)
            )

            drawRoundRect(
                color = Color(0xFF00E5FF),
                topLeft = Offset(cardLeft - 3f, cardTop - 3f),
                size = Size(cardWidth + 6f, cardHeight + 6f),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 2f)
            )

            // Center targeting crosshairs
            val centerX = w * 0.48f
            val centerY = h * 0.48f
            drawLine(
                color = Color(0xFF00E676),
                start = Offset(centerX - 14f, centerY),
                end = Offset(centerX + 14f, centerY),
                strokeWidth = 1.5f
            )
            drawLine(
                color = Color(0xFF00E676),
                start = Offset(centerX, centerY - 14f),
                end = Offset(centerX, centerY + 14f),
                strokeWidth = 1.5f
            )
            drawCircle(
                color = Color(0xFF00E676),
                radius = 18f,
                center = Offset(centerX, centerY),
                style = Stroke(width = 1.2f)
            )
        }
    }
}

private fun drawArucoMarker(drawScope: androidx.compose.ui.graphics.drawscope.DrawScope, offset: Offset, size: Float) {
    drawScope.drawRect(
        color = Color.Black,
        topLeft = offset,
        size = Size(size, size)
    )
    drawScope.drawRect(
        color = Color.White,
        topLeft = Offset(offset.x + size * 0.25f, offset.y + size * 0.25f),
        size = Size(size * 0.5f, size * 0.5f)
    )
    drawScope.drawRect(
        color = Color.Black,
        topLeft = Offset(offset.x + size * 0.38f, offset.y + size * 0.38f),
        size = Size(size * 0.24f, size * 0.24f)
    )
}
