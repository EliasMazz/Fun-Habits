package com.yolo.fun_habit_journal.business.usecases.appstart

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.usecases.appstart.usecase.SyncDeletedHabitsUseCase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SyncDeletedHabitsUseCaseTest() {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var cacheDataSource: IHabitCacheDataSource
    private lateinit var networkDataSource: IHabitNetworkDataSource
    private lateinit var factory: HabitFactory

    private lateinit var syncDeletedHabitsUseCase: SyncDeletedHabitsUseCase

    @BeforeEach
    fun setup() {
        with(DependencyContainer().apply { build() }) {
            cacheDataSource = habitCacheDataSource
            networkDataSource = habitNetworkDataSource
            factory = habitFactory
        }

        syncDeletedHabitsUseCase = SyncDeletedHabitsUseCase(
            habitCacheDataSource = cacheDataSource,
            habitNetworkDataSource = networkDataSource
        )
    }

    @Test
    fun `WHEN exists delete network habits that are not deleted from the cache THEN perform delete sync and check if the cache habits were deleted`() =
        runBlocking {
            val networkHabits = networkDataSource.getAllHabits()
            val habitsToDelete: ArrayList<Habit> = ArrayList()
            for (habit in habitsToDelete) {
                habitsToDelete.add(habit)
                networkDataSource.deleteHabit(habit.id)
                networkDataSource.insertOrUpdateHabit(habit)

                val randomNumberOfHabitsToDelete = 4
                if (habitsToDelete.size > randomNumberOfHabitsToDelete) {
                    break
                }
            }

            syncDeletedHabitsUseCase.syncDeletedHabits()

            for (habit in habitsToDelete) {
                val cachedHabit = cacheDataSource.searchHabitById(habit.id)
                assertNull(cachedHabit)
            }
        }
}
