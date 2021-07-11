package com.yolo.fun_habit_journal.business.usecases.appstart

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.business.usecases.appstart.usecase.SyncHabitsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

class SyncHabitsUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var cacheDataSource: IHabitCacheDataSource
    private lateinit var networkDataSource: IHabitNetworkDataSource
    private lateinit var factory: HabitFactory
    private lateinit var dateUtil: DateUtil

    private lateinit var syncHabitsUseCase: SyncHabitsUseCase

    @BeforeEach
    fun setup() {
        with(DependencyContainer().apply { build() }) {
            cacheDataSource = habitCacheDataSource
            networkDataSource = habitNetworkDataSource
            factory = habitFactory
            dateUtil = habitDateUtil
        }

        syncHabitsUseCase = SyncHabitsUseCase(
            habitCacheDataSource = cacheDataSource,
            habitNetworkDataSource = networkDataSource
        )
    }

    @Test
    fun `WHEN insert new habits into the network THEN perform sync and check if the network habits were inserted into the cache`() =
        runBlocking {
            val newHabitList = factory.createHabitList(50)
            networkDataSource.insertOrUpdateListHabit(newHabitList)

            syncHabitsUseCase.syncHabits()

            for (habit in newHabitList) {
                val cachedHabit = cacheDataSource.searchHabitById(habit.id)
                assertNotNull(cachedHabit)
            }
        }

    @Test
    fun `WHEN insert new habits into the cache THEN perform sync and check if the cache habits were inserted into the network`() =
        runBlocking {
            val newHabits = factory.createHabitList(50)
            cacheDataSource.insertHabits(newHabits)

            syncHabitsUseCase.syncHabits()

            for (habit in newHabits) {
                val networkHabit = networkDataSource.searchHabit(habit)
                assertNotNull(networkHabit)
            }
        }

    @Test
    fun `WHEN update new habits into the cache THEN perform sync and check if the cache habits were updated into the network`() =
        runBlocking {
            val cacheHabits = cacheDataSource.searchHabits("", "", 1)
            val habitsToUpdate: ArrayList<Habit> = ArrayList()
            for (habit in cacheHabits) {
                val updatedHabit = factory.createSingleHabit(
                    id = habit.id,
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )

                habitsToUpdate.add(updatedHabit)
                if (habitsToUpdate.size > 6) {
                    break
                }

                cacheDataSource.insertHabits(habitsToUpdate)

                syncHabitsUseCase.syncHabits()

                for (habit in habitsToUpdate) {
                    val networkHabit = networkDataSource.searchHabit(habit)
                    assertEquals(habit.id, networkHabit?.id)
                    assertEquals(habit.title, networkHabit?.title)
                    assertEquals(habit.body, networkHabit?.body)
                    assertEquals(habit.updated_at, networkHabit?.updated_at)
                }
            }
        }

    @Test
    fun `WHEN update new habits into the network THEN perform sync and check if the network habits were updated into the cache`() =
        runBlocking {
            val networkHabits = networkDataSource.getAllHabits()
            val habitsToUpdate: ArrayList<Habit> = ArrayList()
            for (habit in networkHabits) {
                val updatedHabit = factory.createSingleHabit(
                    id = habit.id,
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )

                habitsToUpdate.add(updatedHabit)
                if (habitsToUpdate.size > 4) {
                    break
                }
            }

            networkDataSource.insertOrUpdateListHabit(habitsToUpdate)

            syncHabitsUseCase.syncHabits()

            for (habit in habitsToUpdate) {
                val cacheHabit = cacheDataSource.searchHabitById(habit.id)
                assertEquals(habit.id, cacheHabit?.id)
                assertEquals(habit.title, cacheHabit?.title)
                assertEquals(habit.body, cacheHabit?.body)
                assertEquals(habit.updated_at, cacheHabit?.updated_at)
            }
        }
}
