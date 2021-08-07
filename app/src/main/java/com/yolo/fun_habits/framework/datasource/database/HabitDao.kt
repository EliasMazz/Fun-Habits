package com.yolo.fun_habits.framework.datasource.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: HabitCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitList(habitList: List<HabitCacheEntity>): LongArray

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun searchHabitById(id: String): HabitCacheEntity?

    @Query("DELETE FROM habits WHERE id IN (:ids)")
    suspend fun deleteHabitList(ids: List<String>): Int

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query(
        """
        UPDATE habits 
        SET 
        title = :title, 
        body = :body,
        updated_at = :updated_at
        WHERE id = :primaryKey
        """
    )
    suspend fun updateHabit(
        primaryKey: String,
        title: String,
        body: String?,
        updated_at: String
    ): Int

    @Query("DELETE FROM habits WHERE id = :primaryKey")
    suspend fun deleteHabit(primaryKey: String): Int

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitCacheEntity>


    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitsCount(): Int
}
