package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ExposureDao {
    @Query("SELECT * FROM exposure_readings ORDER BY timestamp ASC")
    fun getAllReadings(): Flow<List<ExposureReadingEntity>>

    @Query("SELECT * FROM exposure_readings ORDER BY timestamp DESC LIMIT 7")
    fun getRecentReadings(): Flow<List<ExposureReadingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReading(reading: ExposureReadingEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<ExposureReadingEntity>)

    @Query("DELETE FROM exposure_readings")
    suspend fun clearAll()
}
