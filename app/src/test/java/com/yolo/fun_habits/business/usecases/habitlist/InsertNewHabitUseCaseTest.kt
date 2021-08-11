package com.yolo.fun_habits.business.usecases.habitlist

import com.yolo.fun_habits.business.data.cache.FORCE_GENERAL_FAILURE
import com.yolo.fun_habits.business.data.cache.FORCE_NEW_HABIT_EXCEPTION
import com.yolo.fun_habits.business.data.cache.FakeHabitCacheDataSourceImpl
import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheErrors
import com.yolo.fun_habits.business.data.network.FakeHabitNetworkDataSourceImpl
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.usecases.habitlist.usecase.INSERT_HABIT_FAILED
import com.yolo.fun_habits.business.usecases.habitlist.usecase.INSERT_HABIT_SUCCESS
import com.yolo.fun_habits.business.usecases.habitlist.usecase.InsertNewHabitUseCase
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@InternalCoroutinesApi
class InsertNewHabitUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: FakeHabitCacheDataSourceImpl
    private lateinit var habitNetworkDataSource: FakeHabitNetworkDataSourceImpl

    private lateinit var habitFactory: HabitFactory

    private lateinit var insertNewHabitUseCase: InsertNewHabitUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource
        habitNetworkDataSource = dependencyContainer.habitNetworkDataSource
        habitFactory = dependencyContainer.habitFactory

        insertNewHabitUseCase = InsertNewHabitUseCase(
            habitCacheDataSource = habitCacheDataSource,
            habitNetworkDataSource = habitNetworkDataSource,
            habitFactory = habitFactory
        )
    }

    @Test
    fun `WHEN insert habit success THEN confirm network and cache updated`() = runBlocking {
        val newHabit = habitFactory.createSingleHabit(
            title = "new habit"
        )

        insertNewHabitUseCase.invoke(
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
        assertEquals(newHabit, cacheHabitThatWasInserted)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(newHabit, networkHabitThatWasInserted)
    }

    @Test
    fun `WHEN insert habit fail THEN confirm network and cache are not updated`() = runBlocking {
        habitCacheDataSource.forceError = FORCE_GENERAL_FAILURE

        val newHabit = habitFactory.createSingleHabit(
            id = UUID.randomUUID().toString(),
            title = "new habit"
        )

        insertNewHabitUseCase.invoke(
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
        assertEquals(null, cacheHabitThatWasInserted)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(null, networkHabitThatWasInserted)
    }

    @Test
    fun `WHEN insert habit throw exception THEN check generic error and confirm network and cache are not updated`() = runBlocking {
        habitCacheDataSource.forceError = FORCE_NEW_HABIT_EXCEPTION

        val newHabit = habitFactory.createSingleHabit(
            id = UUID.randomUUID().toString(),
            title = "new habit"
        )

        insertNewHabitUseCase.invoke(
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
        assertEquals(null, cacheHabitThatWasInserted)

        val networkHabitThatWasInserted = habitNetworkDataSource.searchHabit(newHabit)
        assertEquals(null, networkHabitThatWasInserted)
    }
}
