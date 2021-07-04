package com.yolo.fun_habit_journal.framework.datasource.network.abstraction

import com.yolo.fun_habit_journal.business.domain.model.Habit

interface HabitFirestoreService {

    suspend fun insertOrUpdateHabit(habit: Habit)

    suspend fun deleteHabit(id: String)

    suspend fun insertDeletedHabit(habit: Habit)

    suspend fun insertDeletedHabits(habitList: List<Habit>)

    suspend fun deleteDeletedHabit(habit: Habit)

    suspend fun getDeletedHabitList(): List<Habit>

    suspend fun deleteAllHabits()

    suspend fun searchHabit(habit: Habit): Habit?

    suspend fun getAllHabits(): List<Habit>

    suspend fun insertOrUpdateListHabit(listHabit: List<Habit>)
}
