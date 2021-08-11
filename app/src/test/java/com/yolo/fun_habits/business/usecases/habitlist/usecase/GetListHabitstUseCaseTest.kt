package com.yolo.fun_habits.business.usecases.habitlist.usecase

import com.yolo.fun_habits.business.data.cache.FORCE_GET_ALL_HABITS_EXCEPTION
import com.yolo.fun_habits.business.data.cache.FakeHabitCacheDataSourceImpl
import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheErrors.CACHE_ERROR_UNKNOWN
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class GetListHabitstUseCaseTest() {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var cacheDataSource: FakeHabitCacheDataSourceImpl

    private lateinit var getListHabitstUseCase: GetListHabitstUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }

        cacheDataSource = dependencyContainer.habitCacheDataSource

        getListHabitstUseCase = GetListHabitstUseCase(
            habitCacheDataSource = cacheDataSource
        )
    }

    @Test
    fun `WHEN retrieve habit list from cache error THEN return `() =
        runBlocking {
            cacheDataSource.forceError = FORCE_GET_ALL_HABITS_EXCEPTION

            getListHabitstUseCase.invoke(
                stateEvent = HabitListStateEvent.GetHabitsLisEvent
            ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
                override suspend fun emit(value: DataState<HabitListViewState>?) {
                    Assert.assertTrue(
                        value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN) ?: false
                    )
                }
            })
        }

    @Test
    fun `WHEN retrieve habit list from cache succesfully THEN return success message`() =
        runBlocking {
            getListHabitstUseCase.invoke(
                stateEvent = HabitListStateEvent.GetHabitsLisEvent
            ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
                override suspend fun emit(value: DataState<HabitListViewState>?) {
                    Assert.assertEquals(
                        value?.stateMessage?.response?.message,
                        GET_LIST_HABITS_SUCCESS
                    )
                }
            })
        }

    @Test
    fun `WHEN retrieve habit list from cache and its empty THEN return message empty list of habits`() =
        runBlocking {
            val listOfHabits = cacheDataSource.getAllHabits()
            cacheDataSource.deleteHabits(listOfHabits)

            getListHabitstUseCase.invoke(
                stateEvent = HabitListStateEvent.GetHabitsLisEvent
            ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
                override suspend fun emit(value: DataState<HabitListViewState>?) {
                    Assert.assertEquals(
                        value?.stateMessage?.response?.message,
                        GET_LIST_HABITS_EMPTY
                    )
                }
            })
        }
}

