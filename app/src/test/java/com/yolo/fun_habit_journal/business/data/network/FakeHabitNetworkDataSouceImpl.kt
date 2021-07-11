package com.yolo.fun_habit_journal.business.data.network

import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.util.printLogD

class FakeHabitNetworkDataSourceImpl
constructor(
    private val habitsData: HashMap<String, Habit>,
    private val deletedHabitsData: HashMap<String, Habit>,
    private val dateUtil: DateUtil
) : IHabitNetworkDataSource {

    override suspend fun insertOrUpdateHabit(habit: Habit) {
        val habitUpdated = habit.copy(updated_at = dateUtil.getCurrentTimestamp())
        habitsData[habitUpdated.id] = habitUpdated
    }

    override suspend fun deleteHabit(id: String) {
        habitsData.remove(id)
    }

    override suspend fun searchHabit(habit: Habit): Habit? {
        return habitsData.get(habit.id)
    }

    override suspend fun getAllHabits(): List<Habit> {
        return ArrayList(habitsData.values)
    }

    override suspend fun insertOrUpdateListHabit(listHabit: List<Habit>) {
        for (habit in listHabit) {
            habitsData[habit.id] = habit
        }
    }

    override suspend fun insertDeletedHabit(habit: Habit) {
        deletedHabitsData[habit.id] = habit
    }

    override suspend fun insertDeletedHabitList(habitList: List<Habit>) {
        for (habit in habitList) {
            deletedHabitsData[habit.id] = habit
        }
    }

    override suspend fun deleteDeletedHabit(habit: Habit) {
        deletedHabitsData.remove(habit.id)
    }

    override suspend fun getDeletedHabitList(): List<Habit> {
        return ArrayList(deletedHabitsData.values)
    }

    override suspend fun deleteAllHabits() {
        deletedHabitsData.clear()
    }
}
