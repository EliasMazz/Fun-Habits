package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.FORCE_GENERAL_FAILURE
import com.yolo.fun_habit_journal.business.data.cache.FORCE_NEW_HABIT_EXCEPTION
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheErrors
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.usecases.habitlist.InsertNewHabitUseCase.Companion.INSERT_HABIT_FAILED
import com.yolo.fun_habit_journal.business.usecases.habitlist.InsertNewHabitUseCase.Companion.INSERT_HABIT_SUCCESS
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class InsertNewHabitUseCaseTest {
    private val dependencyContainer: DependencyContainer = DependencyContainer().apply { build() }
    private val habitCacheDataSource: IHabitCacheDataSource = dependencyContainer.habitCacheDataSource
    private val habitNetworkDataSource: IHabitNetworkDataSource = dependencyContainer.habitNetworkDataSource
    private val habitFactory: HabitFactory = dependencyContainer.habitFactory

    private val insertNewHabitUseCase = InsertNewHabitUseCase(
        habitCacheDataSource = habitCacheDataSource,
        habitNetworkDataSouce = habitNetworkDataSource,
        habitFactory = habitFactory
    )

    @Test
    fun `WHEN insert habit success THEN confirm network and cache updated`() = runBlocking {
        val newHabit = habitFactory.createSingleHabit(
            title = "new habit"
        )

        insertNewHabitUseCase.insertNewHabit(
            id = newHabit.id,
            title = newHabit.title,
            stateEvent = HabitListStateEvent.InsertNewHabitEvent(
                title = newHabit.title
            )
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(value?.stateMessage?.response?.message, INSERT_HABIT_SUCCESS)
            }
        })

        val cacheHabitThatWasInserted = habitCacheDataSource.searchHabitById(newHabit.id)
        assertEquals(cacheHabitThatWasInserted, newHabit)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(networkHabitThatWasInserted, newHabit)
    }

    @Test
    fun `WHEN insert habit fail THEN confirm network and cache are not updated`() = runBlocking {
        val newHabit = habitFactory.createSingleHabit(
            id = FORCE_GENERAL_FAILURE,
            title = "new habit"
        )

        insertNewHabitUseCase.insertNewHabit(
            id = newHabit.id,
            title = newHabit.title,
            stateEvent = HabitListStateEvent.InsertNewHabitEvent(
                title = newHabit.title
            )
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(value?.stateMessage?.response?.message, INSERT_HABIT_FAILED)
            }
        })

        val cacheHabitThatWasInserted = habitCacheDataSource.searchHabitById(newHabit.id)
        assertEquals(cacheHabitThatWasInserted, null)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(networkHabitThatWasInserted, null)
    }

    @Test
    fun `WHEN insert habit throw exception THEN check generic error and confirm network and cache are not updated`() = runBlocking {
        val newHabit = habitFactory.createSingleHabit(
            id = FORCE_NEW_HABIT_EXCEPTION,
            title = "new habit"
        )

        insertNewHabitUseCase.insertNewHabit(
            id = newHabit.id,
            title = newHabit.title,
            stateEvent = HabitListStateEvent.InsertNewHabitEvent(
                title = newHabit.title
            )
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assert(value?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false)
            }
        })

        val cacheHabitThatWasInserted = habitCacheDataSource.searchHabitById(newHabit.id)
        assertEquals(cacheHabitThatWasInserted, null)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(networkHabitThatWasInserted, null)
    }
}
