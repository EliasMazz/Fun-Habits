package com.yolo.fun_habit_journal.business.usecases.appstart

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.collections.ArrayList

class SyncHabitsUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: IHabitCacheDataSource
    private lateinit var habitNetworkDataSource: IHabitNetworkDataSource
    private lateinit var habitFactory: HabitFactory

    private lateinit var syncHabitsUseCase: SyncHabitsUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource
        habitNetworkDataSource = dependencyContainer.habitNetworkDataSource
        habitFactory = dependencyContainer.habitFactory

        syncHabitsUseCase = SyncHabitsUseCase(
            habitCacheDataSource = habitCacheDataSource,
            habitNetworkDataSource = habitNetworkDataSource
        )
    }

    @Test
    fun `WHEN insert new habits into the network and perform sync THEN check if the network habits were inserted into the cache`() =
        runBlocking {
            val newHabitList = habitFactory.createHabitList(50)
            habitNetworkDataSource.insertOrUpdateListHabit(newHabitList)

            syncHabitsUseCase.syncHabits()

            for (habit in newHabitList) {
                val cachedHabit = habitCacheDataSource.searchHabitById(habit.id)
                assertNotNull(cachedHabit)
            }
        }

    @Test
    fun `WHEN insert new habits into the cache and perform sync THEN check if the cache habits were inserted into the network`() =
        runBlocking {
            val newHabits = habitFactory.createHabitList(50)
            habitCacheDataSource.insertHabits(newHabits)

            syncHabitsUseCase.syncHabits()

            for (habit in newHabits) {
                val networkHabit = habitNetworkDataSource.searchHabit(habit)
                assertNotNull(networkHabit)
            }
        }

    @Test
    fun `WHEN update new habits into the cache and perform sync THEN check if the cache habits were updated into the network`() =
        runBlocking {
            val cacheHabits = habitCacheDataSource.searchHabits("", "", 1)
            val habitsToUpdate: ArrayList<Habit> = ArrayList()
            for (habit in cacheHabits) {
                val updatedHabit = habitFactory.createSingleHabit(
                    id = habit.id,
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )

                habitsToUpdate.add(updatedHabit)
                if (habitsToUpdate.size > 6) {
                    break
                }

                habitCacheDataSource.insertHabits(habitsToUpdate)

                syncHabitsUseCase.syncHabits()

                for (habit in habitsToUpdate) {
                    val networkHabit = habitNetworkDataSource.searchHabit(habit)
                    assertEquals(habit.id, networkHabit?.id)
                    assertEquals(habit.title, networkHabit?.title)
                    assertEquals(habit.body, networkHabit?.body)
                    assertEquals(habit.updated_at, networkHabit?.updated_at)
                }
            }
        }

    @Test
    fun `WHEN update new habits into the network and perform sync THEN check if the network habits were updated into the cache`() =
        runBlocking {
            val networkHabits = habitNetworkDataSource.getAllHabits()
            val habitsToUpdate: ArrayList<Habit> = ArrayList()
            for (habit in networkHabits) {
                val updatedHabit = habitFactory.createSingleHabit(
                    id = habit.id,
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )

                habitsToUpdate.add(updatedHabit)
                if (habitsToUpdate.size > 4) {
                    break
                }
            }

            habitNetworkDataSource.insertOrUpdateListHabit(habitsToUpdate)

            syncHabitsUseCase.syncHabits()

            for (habit in habitsToUpdate) {
                val cacheHabit = habitCacheDataSource.searchHabitById(habit.id)
                assertEquals(habit.id, cacheHabit?.id)
                assertEquals(habit.title, cacheHabit?.title)
                assertEquals(habit.body, cacheHabit?.body)
                assertEquals(habit.updated_at, cacheHabit?.updated_at)
            }
        }
}
