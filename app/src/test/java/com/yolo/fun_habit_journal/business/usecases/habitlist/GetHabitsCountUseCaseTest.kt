package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.collect

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test


@InternalCoroutinesApi
class GetHabitsCountUseCaseTest {
    private val dependencyContainer: DependencyContainer = DependencyContainer().apply { build() }
    private val habitsCacheDataSource: IHabitCacheDataSource = dependencyContainer.habitCacheDataSource

    private val getHabitsCountUseCase = GetHabitsCountUseCase(
        habitsCacheDataSource = habitsCacheDataSource
    )

    @Test
    fun `WHEN get habits count is called THEN return success confirm correct`() = runBlocking {
        var habitsCount = 0

        getHabitsCountUseCase.getHabitsCount(
            stateEvent = HabitListStateEvent.GetHabitsCountInCacheEvent
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    GET_HABITS_COUNT_SUCCESS
                )

                habitsCount = value?.data?.habitsCountInCache ?: 0
            }
        })

        val expectedHabitsCountInCache = habitsCacheDataSource.getHabitsCount()
        assertTrue(expectedHabitsCountInCache == habitsCount)
    }
}
