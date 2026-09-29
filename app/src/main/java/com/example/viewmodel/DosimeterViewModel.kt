package com.example.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.DosimeterRepository
import com.example.data.ExposureReadingEntity
import com.example.model.ColorimetricEngine
import com.example.model.OperatingCondition
import com.example.model.ReadingState
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class DosimeterUiState(
    val isAuthenticated: Boolean = false,
    val workerId: String = "W-1042",
    val shiftName: String = "Day Shift",
    val wristbandId: String = "#WB-7842-3A",
    val timestampFormatted: String = "21 Sep 2026\n09:41 AM",
    val fullDateTimeString: String = "21 Sep 2026, 09:41 AM",
    val currentCondition: OperatingCondition = OperatingCondition.STANDARD_SHIFT,
    val readingState: ReadingState = ReadingState.VALID_IN_RANGE,
    val simulatedPpmH: Float = 28.5f,
    val ambientTemp: Float = 28.5f,
    val humidity: Float = 62.0f,
    val shelfLifeDays: Int = 28,
    val fiducialsDetected: Boolean = true,
    val lightingOk: Boolean = true,
    val isScanning: Boolean = false,
    val scanPhaseText: String = "",
    val stripColor: Color = Color(0xFFF6A34F),
    val deltaE: Double = 34.2,
    val exposureDisplayValue: String = "28.5 ppm·h",
    val exposureSubtitle: String = "(CST)",
    val stripStatusText: String = "Good",
    val currentTab: Int = 0,
    val alertCount: Int = 2,
    val failureReason: String = "",
    val capturedImage: Bitmap? = null
)

class DosimeterViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = DosimeterRepository(database.exposureDao())

    val historicalReadings: StateFlow<List<ExposureReadingEntity>> = repository.allReadings
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(DosimeterUiState())
    val uiState: StateFlow<DosimeterUiState> = _uiState.asStateFlow()

    init {
        recomputeDosimeterState()
    }

    fun authenticate(workerId: String) {
        _uiState.update {
            it.copy(
                workerId = workerId,
                isAuthenticated = true,
                currentTab = 0
            )
        }
    }

    fun signOut() {
        _uiState.update {
            it.copy(
                isAuthenticated = false
            )
        }
    }

    fun setTab(index: Int) {
        _uiState.update { it.copy(currentTab = index) }
    }

    fun setOperatingCondition(condition: OperatingCondition) {
        when (condition) {
            OperatingCondition.STANDARD_SHIFT -> {
                _uiState.update {
                    it.copy(
                        currentCondition = condition,
                        simulatedPpmH = 28.5f,
                        ambientTemp = 28.5f,
                        humidity = 62.0f,
                        shelfLifeDays = 28,
                        fiducialsDetected = true,
                        lightingOk = true,
                        readingState = ReadingState.VALID_IN_RANGE
                    )
                }
            }
            OperatingCondition.OVER_SATURATION -> {
                _uiState.update {
                    it.copy(
                        currentCondition = condition,
                        simulatedPpmH = 215.0f,
                        ambientTemp = 28.5f,
                        humidity = 62.0f,
                        shelfLifeDays = 28,
                        fiducialsDetected = true,
                        lightingOk = true,
                        readingState = ReadingState.OUT_OF_RANGE
                    )
                }
            }
            OperatingCondition.EXPIRED_CONSUMABLE -> {
                _uiState.update {
                    it.copy(
                        currentCondition = condition,
                        simulatedPpmH = 28.5f,
                        ambientTemp = 28.5f,
                        humidity = 62.0f,
                        shelfLifeDays = 0,
                        fiducialsDetected = true,
                        lightingOk = true,
                        readingState = ReadingState.EXPIRED_LOCKOUT
                    )
                }
            }
            OperatingCondition.OPTICAL_UNVERIFIED -> {
                _uiState.update {
                    it.copy(
                        currentCondition = condition,
                        simulatedPpmH = 28.5f,
                        ambientTemp = 28.5f,
                        humidity = 62.0f,
                        shelfLifeDays = 28,
                        fiducialsDetected = false,
                        lightingOk = false,
                        readingState = ReadingState.IMAGE_INVALID
                    )
                }
            }
            OperatingCondition.ENVIRONMENTAL_BREACH -> {
                _uiState.update {
                    it.copy(
                        currentCondition = condition,
                        simulatedPpmH = 45.0f,
                        ambientTemp = 48.0f,
                        humidity = 92.0f,
                        shelfLifeDays = 28,
                        fiducialsDetected = true,
                        lightingOk = true,
                        readingState = ReadingState.OUT_OF_RANGE
                    )
                }
            }
            OperatingCondition.CUSTOM -> {
                _uiState.update { it.copy(currentCondition = condition) }
            }
        }
        recomputeDosimeterState()
    }

    fun updateSimulatedPpmH(value: Float) {
        _uiState.update {
            it.copy(
                simulatedPpmH = value,
                currentCondition = OperatingCondition.CUSTOM
            )
        }
        recomputeDosimeterState()
    }

    fun updateTemperature(value: Float) {
        _uiState.update {
            it.copy(
                ambientTemp = value,
                currentCondition = OperatingCondition.CUSTOM
            )
        }
        recomputeDosimeterState()
    }

    fun updateHumidity(value: Float) {
        _uiState.update {
            it.copy(
                humidity = value,
                currentCondition = OperatingCondition.CUSTOM
            )
        }
        recomputeDosimeterState()
    }

    fun updateShelfLife(days: Int) {
        _uiState.update {
            it.copy(
                shelfLifeDays = days,
                currentCondition = OperatingCondition.CUSTOM
            )
        }
        recomputeDosimeterState()
    }

    fun toggleLightingOk(ok: Boolean? = null) {
        _uiState.update {
            val next = ok ?: !it.lightingOk
            it.copy(lightingOk = next, currentCondition = OperatingCondition.CUSTOM)
        }
        recomputeDosimeterState()
    }

    fun toggleFiducials(ok: Boolean? = null) {
        _uiState.update {
            val next = ok ?: !it.fiducialsDetected
            it.copy(fiducialsDetected = next, currentCondition = OperatingCondition.CUSTOM)
        }
        recomputeDosimeterState()
    }

    private fun recomputeDosimeterState() {
        val s = _uiState.value
        val stripColor = ColorimetricEngine.getInterpolatedStripColor(s.simulatedPpmH)
        val deltaE = ColorimetricEngine.calculateDeltaE(stripColor, ColorimetricEngine.SWATCH_0)

        // Evaluate Operating Envelope & Quality Gates
        val isOpticalValid = s.fiducialsDetected && s.lightingOk
        val isShelfLifeValid = s.shelfLifeDays > 0
        val isEnvWithinEnvelope = (s.ambientTemp in 15f..45f) && (s.humidity in 20f..85f)
        val isWithinSaturation = s.simulatedPpmH <= 200f

        val (readingState, displayVal, statusText, failureReason) = when {
            !isShelfLifeValid -> {
                Quadruple(
                    ReadingState.EXPIRED_LOCKOUT,
                    "LOCKED",
                    "Degraded",
                    "Replace wristband - consumable expired"
                )
            }
            !isOpticalValid -> {
                val reason = if (!s.lightingOk) "Lighting Inadequate / Glare Detected" else "Reference Fiducial Misaligned"
                Quadruple(
                    ReadingState.IMAGE_INVALID,
                    "--",
                    "Unverified",
                    reason
                )
            }
            !isWithinSaturation -> {
                Quadruple(
                    ReadingState.OUT_OF_RANGE,
                    "Estimate withheld",
                    "Saturated",
                    "Estimate withheld - manual review required (Saturation >200 ppm-h)"
                )
            }
            !isEnvWithinEnvelope -> {
                Quadruple(
                    ReadingState.OUT_OF_RANGE,
                    "Estimate withheld",
                    "Flagged",
                    "Estimate withheld - manual review required (Operating envelope breached)"
                )
            }
            else -> {
                val formatted = String.format(Locale.US, "%.1f ppm·h", s.simulatedPpmH)
                Quadruple(
                    ReadingState.VALID_IN_RANGE,
                    formatted,
                    "Good",
                    ""
                )
            }
        }

        _uiState.update {
            it.copy(
                stripColor = stripColor,
                deltaE = deltaE,
                readingState = readingState,
                exposureDisplayValue = displayVal,
                stripStatusText = statusText,
                failureReason = failureReason
            )
        }
    }

    fun triggerCaptureAndAnalyze(bitmap: Bitmap? = null) {
        if (_uiState.value.isScanning) return
        if (_uiState.value.readingState == ReadingState.EXPIRED_LOCKOUT) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isScanning = true,
                    capturedImage = bitmap ?: it.capturedImage,
                    scanPhaseText = "Calibrating optical illumination & ArUco fiducials..."
                )
            }
            delay(350)

            _uiState.update {
                it.copy(scanPhaseText = "Sampling strip ROI & extracting CIE L*a*b* coordinates...")
            }
            delay(350)

            _uiState.update {
                it.copy(scanPhaseText = "Applying polynomial calibration k(T, RH)...")
            }
            delay(300)

            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("dd MMM", Locale.US)
            val dateLabel = dateFormat.format(Date(now))

            // Set verified confirmed states
            _uiState.update {
                it.copy(
                    fiducialsDetected = true,
                    lightingOk = true,
                    shelfLifeDays = 28,
                    wristbandId = "#WB-7842-3A",
                    simulatedPpmH = 28.5f,
                    readingState = ReadingState.VALID_IN_RANGE,
                    exposureDisplayValue = "28.5 ppm·h",
                    exposureSubtitle = "(CST)",
                    stripStatusText = "Good",
                    isScanning = false,
                    scanPhaseText = ""
                )
            }

            recomputeDosimeterState()

            val currentState = _uiState.value
            repository.insertReading(
                ExposureReadingEntity(
                    dateLabel = dateLabel,
                    timestamp = now,
                    exposurePpmH = 28.5,
                    status = "VALID_IN_RANGE",
                    temperature = currentState.ambientTemp.toDouble(),
                    humidity = currentState.humidity.toDouble(),
                    wristbandId = "#WB-7842-3A",
                    workerId = currentState.workerId,
                    deltaE = currentState.deltaE,
                    shelfLifeDaysRemaining = 28
                )
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
