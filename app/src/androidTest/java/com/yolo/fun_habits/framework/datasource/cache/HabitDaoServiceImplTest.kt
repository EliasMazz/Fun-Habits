package com.yolo.fun_habits.framework.datasource.cache

import androidx.test.internal.runner.junit4.AndroidJUnit4ClassRunner
import com.yolo.fun_habits.BaseTest
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.di.TestAppComponent
import com.yolo.fun_habits.framework.datasource.cache.abstraction.IHabitDaoService
import com.yolo.fun_habits.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habits.framework.datasource.data.HabitDataFactory
import com.yolo.fun_habits.framework.datasource.database.HabitDao
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@FlowPreview
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4ClassRunner::class)
class HabitDaoServiceImplTest : BaseTest() {

    var habitDaoServiceImpl: IHabitDaoService

    @Inject
    lateinit var habitDao: HabitDao

    @Inject
    lateinit var habitMapperHabit: HabitCacheMapper

    @Inject
    lateinit var dateUtil: DateUtil

    @Inject
    lateinit var habitDataFactory: HabitDataFactory

    override fun injectTest() {
        (application.appComponent as TestAppComponent).inject(this)
    }

    init {
        injectTest()
        habitDaoServiceImpl = HabitDaoServiceImpl(
            habitDao,
            habitMapperHabit,
            dateUtil
        )
        insertTestData()
    }

    private fun insertTestData() = runBlocking {
        val entityList = habitDataFactory.produceListOfHabits()
            .map { habitMapperHabit.mapToEntity(it) }

        habitDao.insertHabitList(entityList)
    }

    @Test
    fun a_searchHabits_confirmDbNotEmpty() = runBlocking {
        val numHabits = habitDaoServiceImpl.getHabitsCount()
        assertTrue { numHabits > 0 }
    }

    @Test
    fun insertHabit_confirmInserted() = runBlocking {
        val newHabit = habitDataFactory.createSingleHabit(
            id = null,
            title = "title",
            body = "body"
        )

        habitDaoServiceImpl.insertHabit(newHabit)

        val result = habitDaoServiceImpl.getAllHabits()
        assertTrue { result.contains(newHabit) }
    }

    @Test
    fun insertHabitList_confirmInserted() = runBlocking {
        val habitList = habitDataFactory.createHabitList(10)

        habitDaoServiceImpl.insertHabitList(habitList)

        val result = habitDaoServiceImpl.getAllHabits()
        assertTrue { result.containsAll(habitList) }
    }

    @Test
    fun deleteHabit_confirmDeleted() = runBlocking {
        val habit = habitDao.getAllHabits().first()

        habitDaoServiceImpl.deleteHabit(habit.id)

        val result = habitDao.getAllHabits()

        assertFalse { result.contains(habit) }
    }

    @Test
    fun deleteHabitList_confirmDeleted() = runBlocking {
        //get random list
        val habitList = habitDao.getAllHabits().take(3)
            .map { habitMapperHabit.mapFromEntity(it) }


        habitDaoServiceImpl.deleteHabitList(habitList)


        val result = habitDao.getAllHabits()
            .map { habitMapperHabit.mapFromEntity(it) }


        assertFalse { result.containsAll(habitList) }
    }


}
