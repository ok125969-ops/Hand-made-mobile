package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    MemoryEntity::class,
    EventLogEntity::class,
    ConversationEntity::class,
    GestureMappingEntity::class,
    GestureHistoryEntity::class,
    GestureSettingsEntity::class,
    GestureCalibrationEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class MyraaDatabase : RoomDatabase() {
  abstract fun memoryDao(): MemoryDao
  abstract fun eventLogDao(): EventLogDao
  abstract fun conversationDao(): ConversationDao
  abstract fun gestureDao(): GestureDao

  companion object {
    @Volatile
    private var INSTANCE: MyraaDatabase? = null

    fun getInstance(context: Context): MyraaDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          MyraaDatabase::class.java,
          "myraa_proactive.db"
        )
          .fallbackToDestructiveMigration(true)
          .build()
        INSTANCE = instance
        instance
      }
    }
  }
}
