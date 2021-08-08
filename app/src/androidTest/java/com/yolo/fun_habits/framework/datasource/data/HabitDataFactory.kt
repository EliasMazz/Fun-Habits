package com.yolo.fun_habits.framework.datasource.data

import android.app.Application
import android.content.res.AssetManager
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.model.HabitFactory
import java.io.IOException
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitDataFactory
@Inject constructor(
    private val application: Application,
    private val habitFactory: HabitFactory
) {
    private fun readJsonFromAsset(filename: String): String? =
        try {
            val inputStream: InputStream = (application.assets as AssetManager)
                .open(filename)
            inputStream.bufferedReader().use { it.readText() }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }

    fun produceListOfHabits(): List<Habit> =
        Gson().fromJson(
            readJsonFromAsset("habit_list.json"),
            object : TypeToken<List<Habit>>() {}.type
        )

    fun produceEmptyListOfHabits(): List<Habit> = ArrayList()

    fun createSingleHabit(
        id: String? = null,
        title: String,
        body: String? = null
    ) = habitFactory.createSingleHabit(
        id = id,
        title = title,
        body = body
    )

    fun createHabitList(numHabits: Int) = habitFactory.createHabitList(numHabits)
}
