package com.yolo.fun_habits.business.usecases.appstart

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.business.usecases.appstart.usecase.SyncHabitsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
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
            val cacheHabits = cacheDataSource.getAllHabits()
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


    @Test
    fun `WHEN update new habit into the network THEN perform sync check if only the updated habit has chaged the updated at date`() =
        runBlocking {
            // update a single habit with new timestamp
            val newDate = dateUtil.getCurrentTimestamp()
            val firstNetworkHabit = networkDataSource.getAllHabits().first()

            val updatedHabit = Habit(
                id = firstNetworkHabit.id,
                title = firstNetworkHabit.title,
                body = firstNetworkHabit.body,
                created_at = firstNetworkHabit.created_at,
                updated_at = newDate
            )

            networkDataSource.insertOrUpdateHabit(updatedHabit)

//        ONLY FOR DEBUG
//        for(habit in networkDataSource.getAllHabits()){
//         println("date: ${habit.updated_at}")
//        }
//        println("BREAK")

            syncHabitsUseCase.syncHabits()

//          Confirm only a single 'updated_at' date was updated
            val habitsInNetowrk = networkDataSource.getAllHabits()
            for (habit in habitsInNetowrk) {
                cacheDataSource.searchHabitById(habit.id)?.let { habit ->
//                ONLY FOR DEBUG
//                println("date: ${habit.updated_at}")
                    if (habit.id == updatedHabit.id) {
                        Assertions.assertTrue { habit.updated_at == newDate }
                    } else {
                        Assertions.assertFalse { habit.updated_at == newDate }
                    }
                }
            }
        }

    @Test
    fun `WHEN update new habit into the network perform sync close app THEN open app and perform sync check if the updated habit is not updated again `() =
        runBlocking {
            // update a single habit with new timestamp
            val newDate = dateUtil.getCurrentTimestamp()
            val firstNetworkHabit = networkDataSource.getAllHabits().first()

            val updatedHabit = Habit(
                id = firstNetworkHabit.id,
                title = firstNetworkHabit.title,
                body = firstNetworkHabit.body,
                created_at = firstNetworkHabit.created_at,
                updated_at = newDate
            )

            networkDataSource.insertOrUpdateHabit(updatedHabit)

            syncHabitsUseCase.syncHabits()

            // simulate launch app again
            delay(1000)
            syncHabitsUseCase.syncHabits()

            // confirm the date was not updated a second time
            val habits = networkDataSource.getAllHabits()
            for (habit in habits) {
                if (habit.id == updatedHabit.id) {
                    assertTrue { habit.updated_at == newDate }
                    break
                }
            }
        }
}
