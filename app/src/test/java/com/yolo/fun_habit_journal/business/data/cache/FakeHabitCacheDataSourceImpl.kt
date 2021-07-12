package com.yolo.fun_habit_journal.business.data.cache

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil

const val FORCE_DELETE_HABIT_EXCEPTION = "FORCE_DELETE_HABIT_EXCEPTION"
const val FORCE_DELETE_HABITS_EXCEPTION = "FORCE_DELETES_HABIT_EXCEPTION"
const val FORCE_UPDATE_HABIT_EXCEPTION = "FORCE_UPDATE_HABIT_EXCEPTION"
const val FORCE_NEW_HABIT_EXCEPTION = "FORCE_NEW_HABIT_EXCEPTION"
const val FORCE_SEARCH_HABITS_EXCEPTION = "FORCE_SEARCH_HABITS_EXCEPTION"
const val FORCE_GENERAL_FAILURE = "FORCE_GENERAL_FAILURE"
const val DEFAULT_SUCCESS_DB_RESULT = 1
const val DEFAULT_FAILURE_DB_RESULT = -1
const val HABIT_PAGINATION_PAGE_SIZE = 30

class FakeHabitCacheDataSourceImpl
constructor(
    private val habitsData: HashMap<String, Habit>,
    private val dateUtil: DateUtil
) : IHabitCacheDataSource {

    override suspend fun insertHabit(habit: Habit): Long {
        if (habit.id == FORCE_NEW_HABIT_EXCEPTION) {
            throw Exception("Something went wrong inserting the habit.")
        }
        if (habit.id == FORCE_GENERAL_FAILURE) {
            return DEFAULT_FAILURE_DB_RESULT.toLong()
        }
        habitsData[habit.id] = habit
        return DEFAULT_SUCCESS_DB_RESULT.toLong()
    }

    override suspend fun deleteHabit(id: String): Int {
        if (id == FORCE_DELETE_HABIT_EXCEPTION) {
            throw Exception("Something went wrong deleting the habit.")
        } else if (id == FORCE_DELETE_HABITS_EXCEPTION) {
            throw Exception("Something went wrong deleting the list of habits.")
        }
        return habitsData.remove(id)?.let { DEFAULT_SUCCESS_DB_RESULT } ?: DEFAULT_FAILURE_DB_RESULT
    }

    override suspend fun deleteHabits(habitList: List<Habit>): Int {
        var failOrSuccess = DEFAULT_SUCCESS_DB_RESULT
        for (habit in habitList) {
            if (habitsData.remove(habit.id) == null) {
                failOrSuccess = DEFAULT_FAILURE_DB_RESULT
            }
        }
        return failOrSuccess
    }

    override suspend fun updateHabit(
        id: String,
        title: String,
        body: String?,
        timestamp: String?
    ): Int {
        if (id == FORCE_UPDATE_HABIT_EXCEPTION) {
            throw Exception("Something went wrong updating the habit.")
        }
        val updatedHabit = Habit(
            id = id,
            title = title,
            body = body ?: "",
            updated_at = timestamp ?: dateUtil.getCurrentTimestamp(),
            created_at = habitsData.get(id)?.created_at ?: dateUtil.getCurrentTimestamp()
        )
        return habitsData.get(id)?.let {
            habitsData[id] = updatedHabit
            DEFAULT_SUCCESS_DB_RESULT
        } ?: DEFAULT_FAILURE_DB_RESULT
    }

    override suspend fun getAllHabits(): List<Habit> {
        return ArrayList(habitsData.values)
    }

    override suspend fun searchHabitById(id: String): Habit? {
        return habitsData.get(id)
    }

    override suspend fun getHabitsCount(): Int {
        return habitsData.size
    }

    override suspend fun insertHabits(habits: List<Habit>): LongArray {
        val results = LongArray(habits.size)
        for ((index, habit) in habits.withIndex()) {
            results[index] = 1
            habitsData[habit.id] = habit
        }
        return results
    }
}
