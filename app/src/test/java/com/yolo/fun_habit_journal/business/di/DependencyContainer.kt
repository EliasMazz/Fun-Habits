package com.yolo.fun_habit_journal.business.di

import com.yolo.fun_habit_journal.business.data.HabitDataFactory
import com.yolo.fun_habit_journal.business.data.cache.FakeHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.FakeHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)
    private val dateUtil = DateUtil(dateFormat)

    lateinit var habitNetworkDataSource: IHabitNetworkDataSource
    lateinit var habitCacheDataSource: IHabitCacheDataSource
    lateinit var habitFactory: HabitFactory

    init {
        isUnitTest = true
    }

    fun build() {
        habitFactory = HabitFactory(dateUtil)
        habitNetworkDataSource = FakeHabitNetworkDataSource(
            habitsData = HashMap(),
            deletedHabitsData = HashMap(),
            dateUtil = dateUtil
        )

        habitCacheDataSource = FakeHabitCacheDataSource(
            habitsData = HashMap(),
            dateUtil = dateUtil
        )
    }
}
