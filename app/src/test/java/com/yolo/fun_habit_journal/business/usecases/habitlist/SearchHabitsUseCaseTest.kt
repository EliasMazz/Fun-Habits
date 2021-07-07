package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.FORCE_SEARCH_HABITS_EXCEPTION
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheErrors
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.framework.datasource.database.ORDER_BY_ASC_DATE_UPDATED
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent.SearchHabitsEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class SearchHabitsUseCaseTest {

    private val dependencyContainer: DependencyContainer = DependencyContainer().apply { build() }
    private val habitCacheDataSource: IHabitCacheDataSource = dependencyContainer.habitCacheDataSource

    private val searchHabitsUseCase = SearchHabitsUseCase(
        habitCacheDataSource = habitCacheDataSource
    )


    @Test
    fun `WHEN blank query is inserted THEN return success confirm habits retrieved`() = runBlocking {
        val query = ""
        var results: ArrayList<Habit>? = null

        searchHabitsUseCase.searchHabits(
            query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchHabitsEvent()
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_HABITS_SUCCESS
                )
                value?.data?.habitList?.let {
                    results = ArrayList(it)
                }
            }
        })

        assertNotNull(results)

        val habitsInCache = habitCacheDataSource.searchHabits(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )

        assertTrue(results?.containsAll(habitsInCache) ?: false)
    }

    @Test
    fun `WHEN non existense query is inserted THEN return success confirm no habits retrieved`() = runBlocking {
        val query = "Asdf asdf"
        var results: ArrayList<Habit>? = null

        searchHabitsUseCase.searchHabits(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchHabitsEvent()
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    value?.stateMessage?.response?.message,
                    SEARCH_HABITS_NO_MATCHING_RESULTS
                )
                value?.data?.habitList?.let { list ->
                    results = ArrayList(list)
                }
            }
        })


        assertTrue { results?.run { size == 0 } ?: true }

        val habitsInCache = habitCacheDataSource.searchHabits(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { habitsInCache.isNotEmpty() }
    }

    @Test
    fun `WHEN search note throws exception THEN return fail confirm no habits retrieved`() = runBlocking {

        val query = FORCE_SEARCH_HABITS_EXCEPTION
        var results: ArrayList<Habit>? = null

        searchHabitsUseCase.searchHabits(
            query = query,
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1,
            stateEvent = SearchHabitsEvent()
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message
                        ?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false
                )
                value?.data?.habitList?.let { list ->
                    results = ArrayList(list)
                }
                println("results: ${results}")
            }
        })

        assertTrue { results?.run { size == 0 } ?: true }

        val habitsInCache = habitCacheDataSource.searchHabits(
            query = "",
            filterAndOrder = ORDER_BY_ASC_DATE_UPDATED,
            page = 1
        )
        assertTrue { habitsInCache.isNotEmpty() }
    }

}
