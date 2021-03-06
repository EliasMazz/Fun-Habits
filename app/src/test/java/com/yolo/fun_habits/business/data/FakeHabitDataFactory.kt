package com.yolo.fun_habits.business.data

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.yolo.fun_habits.business.domain.model.Habit

class FakeHabitDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfHabits(): List<Habit> =
        Gson().fromJson(
            getHabitsFromFile("habit_list.json"),
            object : TypeToken<List<Habit>>() {}.type
        )

    fun produceHashMapOfHabits(habitList: List<Habit>): HashMap<String, Habit> {
        val map = HashMap<String, Habit>()
        for (habit in habitList) {
            map[habit.id] = habit
        }
        return map
    }

    fun produceEmptyListOfHabits(): List<Habit> {
        return ArrayList()
    }

    private fun getHabitsFromFile(fileName: String): String {
        return testClassLoader.getResource(fileName).readText()
    }
}







