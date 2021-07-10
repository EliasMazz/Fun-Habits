package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.FORCE_DELETE_HABITS_EXCEPTION
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import org.junit.jupiter.api.Assertions.*
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class DeleteMultipleHabitsUseCaseTest{
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: IHabitCacheDataSource
    private lateinit var habitNetworkDataSource: IHabitNetworkDataSource
    private lateinit var habitFactory: HabitFactory

    private lateinit var deleteMultipleHabitsUseCase: DeleteMultipleHabitsUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource
        habitNetworkDataSource = dependencyContainer.habitNetworkDataSource
        habitFactory = dependencyContainer.habitFactory

        deleteMultipleHabitsUseCase = DeleteMultipleHabitsUseCase(
            habitCacheDataSource = habitCacheDataSource,
            habitNetworkDataSource = habitNetworkDataSource
        )
    }

    @Test
    fun `WHEN delete habits success THEN confirm network and cache updated`() = runBlocking {
        val randomHabits: ArrayList<Habit> = ArrayList()
        val habitsInCache = habitCacheDataSource.searchHabits("", "", 1)

        for (habit in habitsInCache) {
            randomHabits.add(habit)
            if (randomHabits.size > 4) {
                break
            }
        }

        deleteMultipleHabitsUseCase.deleteHabits(
            habitList = randomHabits,
            stateEvent = HabitListStateEvent.DeleteMultipleHabitsEvent(randomHabits)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    DELETE_HABITS_SUCCESS,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val deletedNetworkHabits = habitNetworkDataSource.getDeletedHabitList()
        assertTrue(deletedNetworkHabits.containsAll(randomHabits))

        val doHabitsExistInNetwork = habitNetworkDataSource.getAllHabits().containsAll(randomHabits)
        assertFalse(doHabitsExistInNetwork)

        for (habit in randomHabits) {
            val habitInCache = habitCacheDataSource.searchHabitById(habit.id)
            assertTrue { habitInCache == null }
        }
    }

    @Test
    fun `WHEN delete habits fail THEN confirm correct deletes were made`() = runBlocking {
        val validaHabits: ArrayList<Habit> = ArrayList()
        val invalidHabits: ArrayList<Habit> = ArrayList()
        val habitsInCache = habitCacheDataSource.searchHabits("", "", 1)

        for (index in 0..habitsInCache.size) {
            var habit: Habit
            if (index % 2 == 0) {
                habit = habitFactory.createSingleHabit(
                    title = habitsInCache.get(index).title
                )
                invalidHabits.add(habit)
            } else {
                habit = habitsInCache.get(index)
                validaHabits.add(habit)
            }
            if (invalidHabits.size + validaHabits.size > 4) {
                break
            }
        }

        val habitsToDelete = ArrayList(validaHabits + invalidHabits)

        deleteMultipleHabitsUseCase.deleteHabits(
            habitList = habitsToDelete,
            stateEvent = HabitListStateEvent.DeleteMultipleHabitsEvent(habitsToDelete)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    DELETE_HABITS_FAILURE,
                    value?.stateMessage?.response?.message
                )
            }
        })

        //Confirm ONLY the valid habits were deleted from network
        val networkHabits = habitNetworkDataSource.getAllHabits()
        assertFalse { networkHabits.containsAll(validaHabits) }

        //Confirm ONLY the valid habits are inserted into list of deleted network
        val deletedNetworkHabits = habitNetworkDataSource.getDeletedHabitList()
        assertTrue { deletedNetworkHabits.containsAll(validaHabits) }
        assertFalse { deletedNetworkHabits.containsAll(invalidHabits) }

        //Confirm ONLY the valid habits are deleted from the cache
        for (habit in validaHabits) {
            val habitInCache = habitCacheDataSource.searchHabitById(habit.id)
            assertTrue { habitInCache == null }
        }

        val numHabitsInCache = habitCacheDataSource.getHabitsCount()
        assertTrue { numHabitsInCache == (habitsInCache.size - validaHabits.size) }
    }


    @Test
    fun `WHEN delete habits throw exception THEN check generic error and confirm network and cache are not updated`() = runBlocking {
        val validaHabits: ArrayList<Habit> = ArrayList()
        val invalidHabits: ArrayList<Habit> = ArrayList()
        val habitsInCache = habitCacheDataSource.searchHabits("", "", 1)

        for (habit in habitsInCache) {
            validaHabits.add(habit)
            if (validaHabits.size > 4) {
                break
            }
        }

        val errorHabit = habitFactory.createSingleHabit(
            id = FORCE_DELETE_HABITS_EXCEPTION,
            title = "Habit Exception"
        )

        invalidHabits.add(errorHabit)
        val habitsToDelete = ArrayList(validaHabits + invalidHabits)

        deleteMultipleHabitsUseCase.deleteHabits(
            habitList = habitsToDelete,
            stateEvent = HabitListStateEvent.DeleteMultipleHabitsEvent(habitsToDelete)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    DELETE_HABITS_FAILURE,
                    value?.stateMessage?.response?.message
                )
            }
        })

        //Confirm ONLY the valid habits were deleted from network
        val networkHabits = habitNetworkDataSource.getAllHabits()
        assertFalse { networkHabits.containsAll(validaHabits) }

        //Confirm ONLY the valid habits are inserted into list of deleted network
        val deletedNetworkHabits = habitNetworkDataSource.getDeletedHabitList()
        assertTrue { deletedNetworkHabits.containsAll(validaHabits) }
        assertFalse { deletedNetworkHabits.containsAll(invalidHabits) }

        //Confirm ONLY the valid habits are deleted from the cache
        for (habit in validaHabits) {
            val habitInCache = habitCacheDataSource.searchHabitById(habit.id)
            assertTrue { habitInCache == null }
        }

        val numHabitsInCache = habitCacheDataSource.getHabitsCount()
        assertTrue { numHabitsInCache == (habitsInCache.size - validaHabits.size) }
    }
}
