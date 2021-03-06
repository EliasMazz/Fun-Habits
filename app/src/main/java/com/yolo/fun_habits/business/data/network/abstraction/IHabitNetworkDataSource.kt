package com.yolo.fun_habits.business.data.network.abstraction

import com.yolo.fun_habits.business.domain.model.Habit

interface IHabitNetworkDataSource {

    suspend fun insertOrUpdateHabit(habit: Habit)

    suspend fun deleteHabit(id: String)

    suspend fun insertDeletedHabit(habit: Habit)

    suspend fun insertDeletedHabitList(habitList: List<Habit>)

    suspend fun deleteDeletedHabit(habit: Habit)

    suspend fun getDeletedHabitList(): List<Habit>?

    suspend fun deleteAllHabits()

    suspend fun searchHabit(habit: Habit): Habit?

    suspend fun getAllHabits(): List<Habit>

    suspend fun insertOrUpdateListHabit(listHabit: List<Habit>)
}
