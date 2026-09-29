package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [ExposureReadingEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun exposureDao(): ExposureDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "visidose_h2s_database"
                )
                .addCallback(DatabaseCallback(scope))
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialHistory(database.exposureDao())
                    }
                }
            }
        }

        suspend fun populateInitialHistory(dao: ExposureDao) {
            val now = System.currentTimeMillis()
            val dayMillis = 86_400_000L
            val initial = listOf(
                ExposureReadingEntity(
                    dateLabel = "15 Sep",
                    timestamp = now - (6 * dayMillis),
                    exposurePpmH = 8.2,
                    status = "VALID_IN_RANGE",
                    temperature = 27.8,
                    humidity = 58.0,
                    shelfLifeDaysRemaining = 30
                ),
                ExposureReadingEntity(
                    dateLabel = "16 Sep",
                    timestamp = now - (5 * dayMillis),
                    exposurePpmH = 14.5,
                    status = "VALID_IN_RANGE",
                    temperature = 28.1,
                    humidity = 60.5,
                    shelfLifeDaysRemaining = 29
                ),
                ExposureReadingEntity(
                    dateLabel = "17 Sep",
                    timestamp = now - (4 * dayMillis),
                    exposurePpmH = 15.8,
                    status = "VALID_IN_RANGE",
                    temperature = 29.0,
                    humidity = 64.0,
                    shelfLifeDaysRemaining = 29
                ),
                ExposureReadingEntity(
                    dateLabel = "18 Sep",
                    timestamp = now - (3 * dayMillis),
                    exposurePpmH = 17.2,
                    status = "VALID_IN_RANGE",
                    temperature = 28.4,
                    humidity = 61.2,
                    shelfLifeDaysRemaining = 28
                ),
                ExposureReadingEntity(
                    dateLabel = "19 Sep",
                    timestamp = now - (2 * dayMillis),
                    exposurePpmH = 19.1,
                    status = "VALID_IN_RANGE",
                    temperature = 27.9,
                    humidity = 63.5,
                    shelfLifeDaysRemaining = 28
                ),
                ExposureReadingEntity(
                    dateLabel = "20 Sep",
                    timestamp = now - (1 * dayMillis),
                    exposurePpmH = 22.4,
                    status = "VALID_IN_RANGE",
                    temperature = 28.3,
                    humidity = 62.8,
                    shelfLifeDaysRemaining = 28
                ),
                ExposureReadingEntity(
                    dateLabel = "21 Sep",
                    timestamp = now,
                    exposurePpmH = 28.5,
                    status = "VALID_IN_RANGE",
                    temperature = 28.5,
                    humidity = 62.0,
                    shelfLifeDaysRemaining = 28
                )
            )
            dao.insertAll(initial)
        }
    }
}
