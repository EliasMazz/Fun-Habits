package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.FORCE_GENERAL_FAILURE
import com.yolo.fun_habit_journal.business.data.cache.FORCE_NEW_HABIT_EXCEPTION
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheErrors.CACHE_ERROR_UNKNOWN
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.di.DependencyContainer
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class RestoreDeletedHabitUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var cacheDataSource: IHabitCacheDataSource
    private lateinit var networkDataSource: IHabitNetworkDataSource
    private lateinit var factory: HabitFactory

    private lateinit var restoreDeletedHabitUseCase: RestoreDeletedHabitUseCase

    @BeforeEach
    fun setup() {
        with(DependencyContainer().apply { build() }) {
            cacheDataSource = habitCacheDataSource
            networkDataSource = habitNetworkDataSource
            factory = habitFactory
        }

        restoreDeletedHabitUseCase = RestoreDeletedHabitUseCase(
            habitCacheDataSource = cacheDataSource,
            habitNetworkDataSource = networkDataSource
        )
    }

    @Test
    fun `WHEN restore deleted habit success THEN confirm cache and update network`() = runBlocking {
        val restoreHabit = factory.createSingleHabit(
            title = "Restored Habit"
        )

        networkDataSource.insertDeletedHabit(restoreHabit)

        var deletedHabitList = networkDataSource.getDeletedHabitList()
        assertTrue { deletedHabitList!!.contains(restoreHabit) }

        restoreDeletedHabitUseCase.restoreDeletedHabit(
            habit = restoreHabit,
            stateEvent = HabitListStateEvent.RestoreDeletedHabitEvent(restoreHabit)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    RESTORE_HABIT_SUCCESS,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(restoreHabit.id)
        assertTrue { habitInCache == restoreHabit }

        val habitInNetwork = networkDataSource.searchHabit(restoreHabit)
        assertTrue { habitInNetwork == restoreHabit }

        deletedHabitList = networkDataSource.getDeletedHabitList()
        assertFalse { deletedHabitList!!.contains(restoreHabit) }
    }

    @Test
    fun `WHEN restore deleted habit fail THEN confirm cache and netowrk are not updated`() = runBlocking {
        val restoreHabit = factory.createSingleHabit(
            id = FORCE_GENERAL_FAILURE,
            title = "Restored Habit"
        )

        networkDataSource.insertDeletedHabit(restoreHabit)

        var deletedHabitList = networkDataSource.getDeletedHabitList()
        assertTrue { deletedHabitList!!.contains(restoreHabit) }

        restoreDeletedHabitUseCase.restoreDeletedHabit(
            habit = restoreHabit,
            stateEvent = HabitListStateEvent.RestoreDeletedHabitEvent(restoreHabit)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assertEquals(
                    RESTORE_HABIT_FAILED,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(restoreHabit.id)
        assertFalse { habitInCache == restoreHabit }

        val habitInNetwork = networkDataSource.searchHabit(restoreHabit)
        assertFalse { habitInNetwork == restoreHabit }

        deletedHabitList = networkDataSource.getDeletedHabitList()
        assertTrue { deletedHabitList!!.contains(restoreHabit) }
    }

    @Test
    fun `WHEN restore deleted habit throw exception THEN confirm network and cache are not updated`() = runBlocking {
        val restoreHabit = factory.createSingleHabit(
            id = FORCE_NEW_HABIT_EXCEPTION,
            title = "Restored Habit"
        )

        networkDataSource.insertDeletedHabit(restoreHabit)

        var deletedHabitList = networkDataSource.getDeletedHabitList()
        assertTrue { deletedHabitList!!.contains(restoreHabit) }

        restoreDeletedHabitUseCase.restoreDeletedHabit(
            habit = restoreHabit,
            stateEvent = HabitListStateEvent.RestoreDeletedHabitEvent(restoreHabit)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message?.contains(CACHE_ERROR_UNKNOWN) ?: false
                )
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(restoreHabit.id)
        assertFalse { habitInCache == restoreHabit }

        val habitInNetwork = networkDataSource.searchHabit(restoreHabit)
        assertFalse { habitInNetwork == restoreHabit }

        deletedHabitList = networkDataSource.getDeletedHabitList()
        assertTrue { deletedHabitList!!.contains(restoreHabit) }
    }
}
