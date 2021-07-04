package com.yolo.fun_habit_journal.business.data.cache

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.framework.datasource.cache.abstraction.HabitDaoService
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

    override suspend fun deleteHabits(habitList: List<Habit>) = habitDaoService.deleteHabitList(habitList)

    override suspend fun updateHabit(id: String, title: String, body: String?, timestamp: String?) =
        habitDaoService.updateHabit(id, title, body, timestamp)

    override suspend fun searchHabits(query: String, filterAndOrder: String, page: Int) =
        habitDaoService.returnOrderedQuery(query, filterAndOrder, page)

    override suspend fun searchHabitById(id: String) =
        habitDaoService.searchHabitById(id)

    override suspend fun getHabitsCount() =
        habitDaoService.getHabitCount()

    override suspend fun insertHabits(habits: List<Habit>) =
        habitDaoService.insertHabitList(habits)
}
