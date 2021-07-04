package com.yolo.fun_habit_journal.business.data.cache.abstraction

import com.yolo.fun_habit_journal.business.domain.model.Habit

interface IHabitCacheDataSource {

    suspend fun insertHabit(habit: Habit): Long

    suspend fun deleteHabit(id: String): Int

    suspend fun deleteHabits(habitList: List<Habit>): Int

    suspend fun updateHabit(id: String, title: String, body: String): Int

    suspend fun searchNotes(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Habit>

    suspend fun searchHabitById(id: String): Habit?

    suspend fun getHabitsCount(): Int

    suspend fun insertHabits(habits: List<Habit>): LongArray

}
