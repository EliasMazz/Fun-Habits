package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GET_HABITS_COUNT_SUCCESS
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GetHabitsCountUseCase
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


@InternalCoroutinesApi
class GetHabitsCountUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: IHabitCacheDataSource

    private lateinit var getHabitsCountUseCase: GetHabitsCountUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource

        getHabitsCountUseCase = GetHabitsCountUseCase(
            habitCacheDataSource = habitCacheDataSource
        )
    }

    @Test
    fun `WHEN get habits count is called THEN return success confirm correct`() = runBlocking {
        var habitsCount = 0

        getHabitsCountUseCase.getHabitsCount(
            stateEvent = HabitListStateEvent.GetHabitsCountInCacheEvent
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    GET_HABITS_COUNT_SUCCESS,
                    value?.stateMessage?.response?.message
                )

                habitsCount = value?.data?.habitsCountInCache ?: 0
            }
        })

        val expectedHabitsCountInCache = habitCacheDataSource.getHabitsCount()
        assertTrue(expectedHabitsCountInCache == habitsCount)
    }
}
