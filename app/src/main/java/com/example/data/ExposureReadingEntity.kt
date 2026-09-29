package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exposure_readings")
data class ExposureReadingEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val dateLabel: String,
    val timestamp: Long,
    val exposurePpmH: Double,
    val status: String,
    val temperature: Double,
    val humidity: Double,
    val wristbandId: String = "#WB-7842-3A",
    val workerId: String = "W-1042",
    val deltaE: Double = 0.0,
    val shelfLifeDaysRemaining: Int = 28
)
