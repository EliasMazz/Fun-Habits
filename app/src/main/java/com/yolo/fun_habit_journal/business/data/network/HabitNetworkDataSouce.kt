package com.yolo.fun_habit_journal.business.data.network

import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.framework.datasource.network.abstraction.HabitFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitNetworkDataSouce
@Inject
constructor(
    private val habitFirestoreService: HabitFirestoreService
) : IHabitNetworkDataSource {

    override suspend fun insertOrUpdateHabit(habit: Habit) = habitFirestoreService.insertOrUpdateHabit(habit)

    override suspend fun deleteHabit(id: String) = habitFirestoreService.deleteHabit(id)

    override suspend fun insertDeletedHabit(habit: Habit) = habitFirestoreService.insertDeletedHabit(habit)

    override suspend fun insertDeletedHabitList(habitList: List<Habit>) = habitFirestoreService.insertDeletedHabits(habitList)

    override suspend fun deleteDeletedHabit(habit: Habit) = habitFirestoreService.deleteDeletedHabit(habit)

    override suspend fun getDeletedHabitList() = habitFirestoreService.getDeletedHabitList()

    override suspend fun deleteAllHabits() = habitFirestoreService.deleteAllHabits()

    override suspend fun searchHabit(habit: Habit) = habitFirestoreService.searchHabit(habit)

    override suspend fun getAllHabits() = habitFirestoreService.getAllHabits()

    override suspend fun insertOrUpdateListHabit(listHabit: List<Habit>) = habitFirestoreService.insertOrUpdateListHabit(listHabit)
}
