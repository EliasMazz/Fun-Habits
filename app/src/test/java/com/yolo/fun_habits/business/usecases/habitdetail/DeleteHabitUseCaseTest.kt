package com.yolo.fun_habits.business.usecases.habitdetail

import com.yolo.fun_habits.business.data.cache.FORCE_DELETE_HABIT_EXCEPTION
import com.yolo.fun_habits.business.data.cache.FakeHabitCacheDataSourceImpl
import com.yolo.fun_habits.business.data.cache.util.CacheErrors.CACHE_ERROR_UNKNOWN
import com.yolo.fun_habits.business.data.network.FakeHabitNetworkDataSourceImpl
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.DELETE_HABIT_FAILURE
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.DELETE_HABIT_SUCCESS
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.DeleteHabitUseCase
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@InternalCoroutinesApi
class DeleteHabitUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var habitCacheDataSource: FakeHabitCacheDataSourceImpl
    private lateinit var habitNetworkDataSource: FakeHabitNetworkDataSourceImpl
    private lateinit var habitFactory: HabitFactory

    private lateinit var deleteHabitUseCase: DeleteHabitUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        habitCacheDataSource = dependencyContainer.habitCacheDataSource
        habitNetworkDataSource = dependencyContainer.habitNetworkDataSource
        habitFactory = dependencyContainer.habitFactory

        deleteHabitUseCase = DeleteHabitUseCase(
            habitCacheDataSource = habitCacheDataSource,
            habitNetworkDataSouce = habitNetworkDataSource
        )
    }

    @Test
    fun `WHEN delete habit success THEN confirm network updated`() = runBlocking {
        val habitToDelete = getFirstResultFromCache()

        deleteHabitUseCase.invoke(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
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

        deleteHabitUseCase.invoke(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
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
        habitCacheDataSource.forceError = FORCE_DELETE_HABIT_EXCEPTION

        val habitToDelete = habitFactory.createSingleHabit(
            id = UUID.randomUUID().toString(),
            title = "Title"
        )

        deleteHabitUseCase.invoke(
            habit = habitToDelete,
            stateEvent = HabitDetailStateEvent.DeleteHabitEvent(habitToDelete)
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
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
