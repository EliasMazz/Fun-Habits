package com.yolo.fun_habit_journal.business.di

import com.yolo.fun_habit_journal.business.data.FakeHabitDataFactory
import com.yolo.fun_habit_journal.business.data.cache.FakeHabitCacheDataSourceImpl
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.FakeHabitNetworkDataSourceImpl
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
    lateinit var fakeHabitDataFactory: FakeHabitDataFactory

    private var habitsData: HashMap<String, Habit> = HashMap()

    init {
        isUnitTest = true
    }

    fun build() {
        this.javaClass.classLoader?.let {
            fakeHabitDataFactory = FakeHabitDataFactory(it)
            habitsData = fakeHabitDataFactory.produceHashMapOfHabits(
                fakeHabitDataFactory.produceListOfHabits()
            )
        }

        habitFactory = HabitFactory(dateUtil)
        habitNetworkDataSource = FakeHabitNetworkDataSourceImpl(
            habitsData = habitsData.toMutableMap() as HashMap<String, Habit>,
            deletedHabitsData = HashMap(),
            dateUtil = dateUtil
        )

        habitCacheDataSource = FakeHabitCacheDataSourceImpl(
            habitsData = habitsData.toMutableMap() as HashMap<String, Habit>,
            dateUtil = dateUtil
        )
    }
}
