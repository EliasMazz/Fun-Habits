package com.yolo.fun_habit_journal.business.data.cache

import com.yolo.fun_habit_journal.business.data.cache.`interface`.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitCacheDataSource
@Inject
constructor(
    private val habitDaoService: HabitDaoService
) : IHabitCacheDataSource {

    override suspend fun insertHabit(habit: Habit) = habitDaoService.insertHabit(habit)

    override suspend fun deleteHabit(id: String) = habitDaoService.deleteHabit(id)

    override suspend fun deleteHabits(habitList: List<Habit>) = habitDaoService.deleteHabits(habitList)

    override suspend fun updateHabit(id: String, title: String, body: String) =
        habitDaoService.updateHabit(id, title, body)

    override suspend fun searchNotes(query: String, filterAndOrder: String, page: Int) =
        habitDaoService.searchNotes(query, filterAndOrder, page)

    override suspend fun searchHabitById(id: String) =
        habitDaoService.searchHabitById(id)

    override suspend fun getHabitsCount() =
        habitDaoService.getHabitsCount()

    override suspend fun insertHabits(habits: List<Habit>) =
        habitDaoService.insertHabits(habits)
}
