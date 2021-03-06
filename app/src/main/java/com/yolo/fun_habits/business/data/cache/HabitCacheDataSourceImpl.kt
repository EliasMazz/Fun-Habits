package com.yolo.fun_habits.business.data.cache

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.framework.datasource.cache.abstraction.IHabitDaoService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitCacheDataSourceImpl
@Inject
constructor(
    private val habitDaoService: IHabitDaoService
) : IHabitCacheDataSource {

    override suspend fun insertHabit(habit: Habit) = habitDaoService.insertHabit(habit)

    override suspend fun deleteHabit(id: String) = habitDaoService.deleteHabit(id)

    override suspend fun deleteHabits(habitList: List<Habit>) = habitDaoService.deleteHabitList(habitList)

    override suspend fun updateHabit(id: String, title: String, body: String?, timestamp: String?) =
        habitDaoService.updateHabit(id, title, body, timestamp)

    override suspend fun searchHabitById(id: String) =
        habitDaoService.searchHabitById(id)

    override suspend fun getHabitsCount() =
        habitDaoService.getHabitsCount()

    override suspend fun insertHabits(habits: List<Habit>) =
        habitDaoService.insertHabitList(habits)

    override suspend fun getAllHabits(): List<Habit> =
        habitDaoService.getAllHabits()
}
