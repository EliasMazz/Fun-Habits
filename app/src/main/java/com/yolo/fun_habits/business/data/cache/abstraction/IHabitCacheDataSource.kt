package com.yolo.fun_habits.business.data.cache.abstraction

import com.yolo.fun_habits.business.domain.model.Habit

interface IHabitCacheDataSource {

    suspend fun insertHabit(habit: Habit): Long

    suspend fun deleteHabit(id: String): Int

    suspend fun deleteHabits(habitList: List<Habit>): Int

    suspend fun updateHabit(id: String, title: String, body: String?, timestamp: String?): Int

    suspend fun searchHabitById(id: String): Habit?

    suspend fun getHabitsCount(): Int

    suspend fun insertHabits(habits: List<Habit>): LongArray

    suspend fun getAllHabits(): List<Habit>
}
