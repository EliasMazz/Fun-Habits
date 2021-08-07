package com.yolo.fun_habits.framework.datasource.cache.abstraction

import com.yolo.fun_habits.business.domain.model.Habit

interface IHabitDaoService {
    suspend fun insertHabit(habit: Habit): Long

    suspend fun insertHabitList(habitList: List<Habit>): LongArray

    suspend fun searchHabitById(id: String): Habit?

    suspend fun updateHabit(
        id: String,
        title: String,
        body: String?,
        updatedAt: String?
    ): Int

    suspend fun deleteHabit(id: String): Int

    suspend fun deleteHabitList(habitList: List<Habit>): Int

    suspend fun getAllHabits(): List<Habit>

    suspend fun getHabitsCount(): Int
}
