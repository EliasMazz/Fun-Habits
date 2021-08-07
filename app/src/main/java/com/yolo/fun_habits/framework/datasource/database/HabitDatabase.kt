package com.yolo.fun_habits.framework.datasource.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity

@Database(entities = [HabitCacheEntity::class], version = 1)
abstract class HabitDatabase : RoomDatabase() {

    abstract fun habitDao(): HabitDao

    companion object {
        const val DATABASE_NAME = "habit_db"
    }
}
