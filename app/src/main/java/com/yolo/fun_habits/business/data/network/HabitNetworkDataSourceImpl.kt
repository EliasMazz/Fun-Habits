package com.yolo.fun_habits.business.data.network

import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.framework.datasource.network.abstraction.IHabitFirestoreService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitNetworkDataSourceImpl
@Inject
constructor(
    private val IHabitFirestoreService: IHabitFirestoreService
) : IHabitNetworkDataSource {

    override suspend fun insertOrUpdateHabit(habit: Habit) = IHabitFirestoreService.insertOrUpdateHabit(habit)

    override suspend fun deleteHabit(id: String) = IHabitFirestoreService.deleteHabit(id)

    override suspend fun insertDeletedHabit(habit: Habit) = IHabitFirestoreService.insertDeletedHabit(habit)

    override suspend fun insertDeletedHabitList(habitList: List<Habit>) = IHabitFirestoreService.insertDeletedHabits(habitList)

    override suspend fun deleteDeletedHabit(habit: Habit) = IHabitFirestoreService.deleteDeletedHabit(habit)

    override suspend fun getDeletedHabitList() = IHabitFirestoreService.getDeletedHabitList()

    override suspend fun deleteAllHabits() = IHabitFirestoreService.deleteAllHabits()

    override suspend fun searchHabit(habit: Habit) = IHabitFirestoreService.searchHabit(habit)

    override suspend fun getAllHabits() = IHabitFirestoreService.getAllHabits()

    override suspend fun insertOrUpdateListHabit(listHabit: List<Habit>) = IHabitFirestoreService.insertOrUpdateListHabit(listHabit)
}
