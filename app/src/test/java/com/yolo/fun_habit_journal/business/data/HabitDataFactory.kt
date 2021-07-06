package com.yolo.fun_habit_journal.business.data

import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.yolo.fun_habit_journal.business.domain.model.Habit

class HabitDataFactory(
    private val testClassLoader: ClassLoader
) {

    fun produceListOfHabits(): List<Habit> {
        val habits: List<Habit> =Gson()
            .fromJson(
                getHabitsFromFile("note_list.json"),
                object : TypeToken<List<Habit>>() {}.type
            )

        return habits
    }

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







