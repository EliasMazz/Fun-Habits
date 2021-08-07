package com.yolo.fun_habits.business.usecases.habitdetail

import com.yolo.fun_habits.business.data.cache.FORCE_DELETE_HABIT_EXCEPTION
import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheErrors.CACHE_ERROR_UNKNOWN
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.usecases.common.usecase.DELETE_HABIT_FAILURE
import com.yolo.fun_habits.business.usecases.common.usecase.DELETE_HABIT_SUCCESS
import com.yolo.fun_habits.business.usecases.common.usecase.DeleteHabitUseCase
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class DeleteHabitUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: IHabitCacheDataSource
    private lateinit var habitNetworkDataSource: IHabitNetworkDataSource
    private lateinit var habitFactory: HabitFactory

    private lateinit var deleteHabitUseCase: DeleteHabitUseCase<HabitListViewState>

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource
        habitNetworkDataSource = dependencyContainer.habitNetworkDataSource
        habitFactory = dependencyContainer.habitFactory

        deleteHabitUseCase = DeleteHabitUseCase<HabitListViewState>(
            habitCacheDataSource = habitCacheDataSource,
            habitNetworkDataSouce = habitNetworkDataSource
        )
    }

    @Test
    fun `WHEN delete habit success THEN confirm network updated`() = runBlocking {
        val habitToDelete = getFirstResultFromCache()

        deleteHabitUseCase.deleteHabit(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                Assertions.assertEquals(
                    DELETE_HABIT_SUCCESS,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val wasHabitDeleted = habitNetworkDataSource.getAllHabits().contains(habitToDelete)
        assertFalse { wasHabitDeleted }

        val wasDeletedHabitInserted = habitNetworkDataSource.getDeletedHabitList()!!.contains(habitToDelete)
        assertTrue { wasDeletedHabitInserted }
    }

    @Test
    fun `WHEN delete habit fail THEN confirm network is not updated`() = runBlocking {
        val habitToDelete = habitFactory.createSingleHabit(
            id = "Non existence habit ID",
            title = "Title"
        )

        deleteHabitUseCase.deleteHabit(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                Assertions.assertEquals(
                    DELETE_HABIT_FAILURE,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val habitList = habitNetworkDataSource.getAllHabits()
        val habitsCountInCache = habitCacheDataSource.getHabitsCount()
        assertTrue { habitsCountInCache == habitList.size }

        val wasDeletedHabitInserted = !habitNetworkDataSource.getDeletedHabitList()!!.contains(habitToDelete)
        assertTrue { wasDeletedHabitInserted }
    }

    @Test
    fun `WHEN delete habit throw exception THEN check generic error and confirm network is not updated`() = runBlocking {
        val habitToDelete = habitFactory.createSingleHabit(
            id = FORCE_DELETE_HABIT_EXCEPTION,
            title = "Title"
        )

        deleteHabitUseCase.deleteHabit(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitListViewState>?> {
            override suspend fun emit(value: DataState<HabitListViewState>?) {
                assert(
                    value?.stateMessage?.response?.message?.contains(
                        CACHE_ERROR_UNKNOWN
                    ) ?: false
                )
            }
        })

        val habitList = habitNetworkDataSource.getAllHabits()
        val habitsCountInCache = habitCacheDataSource.getHabitsCount()
        assertTrue { habitsCountInCache == habitList.size }

        val wasDeletedHabitInserted = !habitNetworkDataSource.getDeletedHabitList()!!.contains(habitToDelete)
        assertTrue { wasDeletedHabitInserted }
    }

    private suspend fun getFirstResultFromCache() =
        habitCacheDataSource.getAllHabits().first()
}
