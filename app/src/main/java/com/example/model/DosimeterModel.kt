package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlin.math.pow
import kotlin.math.sqrt

enum class ReadingState {
    VALID_IN_RANGE,
    EXPIRED_LOCKOUT,
    OUT_OF_RANGE,
    IMAGE_INVALID
}

enum class OperatingCondition(val label: String, val description: String) {
    STANDARD_SHIFT(
        label = "Standard In-Envelope Operation",
        description = "Calibrated optical scan (~28.5 ppm-h CST), fiducials verified, badge valid, T=28.5°C, RH=62%"
    ),
    OVER_SATURATION(
        label = "High Exposure Saturation Fail-Safe",
        description = "Chemical strip dark plum (>200 ppm-h), quantitative display withheld for manual review"
    ),
    EXPIRED_CONSUMABLE(
        label = "Shelf-Life Expiry Lockout",
        description = "Service life 0/30 days, status EXPIRED_LOCKOUT, scan disabled until cassette replacement"
    ),
    OPTICAL_UNVERIFIED(
        label = "Optical Verification Failure",
        description = "Non-uniform illumination or misaligned fiducials prompt immediate retake"
    ),
    ENVIRONMENTAL_BREACH(
        label = "Environmental Operating Range Breach",
        description = "Ambient temperature or humidity outside certified bounds, flags reading for safety"
    ),
    CUSTOM(
        label = "Manual Sensor Calibration",
        description = "Dynamic parameter adjustment for telemetry testing"
    )
}

data class LabColor(val l: Double, val a: Double, val b: Double)

object ColorimetricEngine {
    // Reference swatches in Cu-PAN system
    val SWATCH_0 = Color(0xFFFFFFFF)
    val SWATCH_10 = Color(0xFFFDF1AA)
    val SWATCH_50 = Color(0xFFF49342)
    val SWATCH_100 = Color(0xFFA25329)
    val SWATCH_200 = Color(0xFF5E1338)

    fun getInterpolatedStripColor(ppmH: Float): Color {
        val clamped = ppmH.coerceIn(0f, 220f)
        return when {
            clamped <= 10f -> {
                val t = clamped / 10f
                lerpColor(SWATCH_0, SWATCH_10, t)
            }
            clamped <= 50f -> {
                val t = (clamped - 10f) / 40f
                lerpColor(SWATCH_10, SWATCH_50, t)
            }
            clamped <= 100f -> {
                val t = (clamped - 50f) / 50f
                lerpColor(SWATCH_50, SWATCH_100, t)
            }
            clamped <= 200f -> {
                val t = (clamped - 100f) / 100f
                lerpColor(SWATCH_100, SWATCH_200, t)
            }
            else -> {
                val t = ((clamped - 200f) / 20f).coerceIn(0f, 1f)
                lerpColor(SWATCH_200, Color(0xFF38071E), t)
            }
        }
    }

    private fun lerpColor(c1: Color, c2: Color, t: Float): Color {
        val r = c1.red + (c2.red - c1.red) * t
        val g = c1.green + (c2.green - c1.green) * t
        val b = c1.blue + (c2.blue - c1.blue) * t
        return Color(r.coerceIn(0f, 1f), g.coerceIn(0f, 1f), b.coerceIn(0f, 1f), 1f)
    }

    // Convert sRGB to CIE L*a*b* (D65 standard illuminant)
    fun rgbToLab(color: Color): LabColor {
        fun pivotRgb(n: Float): Double {
            val d = n.toDouble()
            return if (d > 0.04045) ((d + 0.055) / 1.055).pow(2.4) else d / 12.92
        }

        val r = pivotRgb(color.red) * 100.0
        val g = pivotRgb(color.green) * 100.0
        val b = pivotRgb(color.blue) * 100.0

        // Observer = 2°, Illuminant = D65
        val x = r * 0.4124 + g * 0.3576 + b * 0.1805
        val y = r * 0.2126 + g * 0.7152 + b * 0.0722
        val z = r * 0.0193 + g * 0.1192 + b * 0.9505

        fun pivotXyz(n: Double): Double {
            return if (n > 0.008856) n.pow(1.0 / 3.0) else (7.787 * n) + (16.0 / 116.0)
        }

        val xr = pivotXyz(x / 95.047)
        val yr = pivotXyz(y / 100.000)
        val zr = pivotXyz(z / 108.883)

        val lVal = ((116.0 * yr) - 16.0).coerceAtLeast(0.0)
        val aVal = 500.0 * (xr - yr)
        val bVal = 200.0 * (yr - zr)

        return LabColor(lVal, aVal, bVal)
    }

    fun calculateDeltaE(c1: Color, c2: Color): Double {
        val lab1 = rgbToLab(c1)
        val lab2 = rgbToLab(c2)
        val dl = lab1.l - lab2.l
        val da = lab1.a - lab2.a
        val db = lab1.b - lab2.b
        return sqrt(dl * dl + da * da + db * db)
    }

    // Environmental correction factor
    fun computeEnvironmentalFactor(tempC: Float, rhPercent: Float): Double {
        val dt = (tempC - 25.0)
        val drh = (rhPercent - 50.0)
        return 1.0 + (0.0032 * dt) + (0.0016 * drh)
    }
}
