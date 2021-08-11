package com.yolo.fun_habits.business.di

import com.yolo.fun_habits.business.data.FakeHabitDataFactory
import com.yolo.fun_habits.business.data.cache.FakeHabitCacheDataSourceImpl
import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.network.FakeHabitNetworkDataSourceImpl
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.framework.util.isUnitTest
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class DependencyContainer {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss a", Locale.ENGLISH)

    lateinit var habitDateUtil: DateUtil
    lateinit var habitNetworkDataSource: FakeHabitNetworkDataSourceImpl
    lateinit var habitCacheDataSource: FakeHabitCacheDataSourceImpl
    lateinit var habitFactory: HabitFactory
    lateinit var fakeHabitDataFactory: FakeHabitDataFactory

    init {
        isUnitTest = true
    }

    fun build() {
        habitDateUtil = DateUtil(dateFormat)
        habitFactory = HabitFactory(habitDateUtil)

        this.javaClass.classLoader?.let {
            fakeHabitDataFactory = FakeHabitDataFactory(it)
        }

        habitNetworkDataSource = FakeHabitNetworkDataSourceImpl(
            habitsData = fakeHabitDataFactory.produceHashMapOfHabits(
                fakeHabitDataFactory.produceListOfHabits()
            ),
            deletedHabitsData = HashMap(),
            dateUtil = habitDateUtil
        )

        habitCacheDataSource = FakeHabitCacheDataSourceImpl(
            habitsData = fakeHabitDataFactory.produceHashMapOfHabits(
                fakeHabitDataFactory.produceListOfHabits()
            ),
            dateUtil = habitDateUtil
        )
    }
}
