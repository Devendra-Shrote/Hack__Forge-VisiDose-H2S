package com.example.data

import kotlinx.coroutines.flow.Flow

class DosimeterRepository(private val exposureDao: ExposureDao) {
    val allReadings: Flow<List<ExposureReadingEntity>> = exposureDao.getAllReadings()
    val recentReadings: Flow<List<ExposureReadingEntity>> = exposureDao.getRecentReadings()

    suspend fun insertReading(reading: ExposureReadingEntity) {
        exposureDao.insertReading(reading)
    }

    suspend fun clearHistory() {
        exposureDao.clearAll()
    }
}
