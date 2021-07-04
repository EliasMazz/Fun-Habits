package com.yolo.fun_habit_journal.framework.datasource.cache.abstraction

import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_PAGINATION_PAGE_SIZE

interface HabitDaoService {
    suspend fun insertHabit(habit: Habit): Long

    suspend fun insertHabitList(habitList: List<Habit>): LongArray

    suspend fun searchHabitById(id: String): Habit?

    suspend fun updateHabit(
        id: String,
        title: String,
        body: String?,
        timestamp: String?
    ): Int

    suspend fun deleteHabit(id: String): Int

    suspend fun deleteHabitList(habitList: List<Habit>): Int

    suspend fun searchHabits(): List<Habit>

    suspend fun getAllHabits(): List<Habit>

    suspend fun searchHabitsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<Habit>

    suspend fun searchNotesOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<Habit>

    suspend fun searchNotesOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<Habit>

    suspend fun searchNotesOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<Habit>

    suspend fun getHabitCount(): Int

    suspend fun returnOrderedQuery(
        query: String,
        filterAndOrder: String,
        page: Int
    ): List<Habit>
}
