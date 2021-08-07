package com.yolo.fun_habits.business.usecases.habitdetail

import com.yolo.fun_habits.business.data.cache.FORCE_UPDATE_HABIT_EXCEPTION
import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheErrors
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.di.DependencyContainer
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.UPDATE_HABIT_FAILED
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.UPDATE_HABIT_SUCCESS
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.UpdateHabitUseCase
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailViewState
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@InternalCoroutinesApi
class UpdateHabitUseCaseTest {
    private lateinit var dependencyContainer: DependencyContainer
    private lateinit var cacheDataSource: IHabitCacheDataSource
    private lateinit var networkDataSource: IHabitNetworkDataSource
    private lateinit var factory: HabitFactory

    private lateinit var updateHabitUseCase: UpdateHabitUseCase

    @BeforeEach
    fun setup() {
        dependencyContainer = DependencyContainer().apply { build() }
        cacheDataSource = dependencyContainer.habitCacheDataSource
        networkDataSource = dependencyContainer.habitNetworkDataSource
        factory = dependencyContainer.habitFactory

        updateHabitUseCase = UpdateHabitUseCase(
            habitCacheDataSource = cacheDataSource,
            habitNetworkDataSource = networkDataSource
        )
    }

    @Test
    fun `WHEN update habit success THEN confirm network and cache are updated`() = runBlocking {

        val randomHabit = cacheDataSource.getAllHabits().first()
        val updateHabit = Habit(
            id = randomHabit.id,
            title = "Updated title",
            body = "Updated body",
            updated_at = dependencyContainer.habitDateUtil.getCurrentTimestamp(),
            created_at = randomHabit.created_at
        )

        updateHabitUseCase.updateHabit(
            habit = updateHabit,
            stateEvent = HabitDetailStateEvent.UpdateHabitEvent()
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
                assertEquals(
                    UPDATE_HABIT_SUCCESS,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(updateHabit.id)
        assertEquals(habitInCache, updateHabit)

        val habitInNetwork = networkDataSource.searchHabit(updateHabit)
        assertEquals(habitInNetwork, updateHabit)
    }

    @Test
    fun `WHEN update habit fail THEN confirm network and cache are not updated`() = runBlocking {
        val updateHabit = factory.createSingleHabit(
            title = "Updated title",
            body = "Updated body"
        )

        updateHabitUseCase.updateHabit(
            habit = updateHabit,
            stateEvent = HabitDetailStateEvent.UpdateHabitEvent()
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
                assertEquals(
                    UPDATE_HABIT_FAILED,
                    value?.stateMessage?.response?.message
                )
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(updateHabit.id)
        assertNotEquals(habitInCache, updateHabit)

        val habitInNetwork = networkDataSource.searchHabit(updateHabit)
        assertNotEquals(habitInNetwork, updateHabit)
    }

    @Test
    fun `WHEN update habit throw an exception THEN confirm network and cache are not updated`() = runBlocking {
        val updateHabit = factory.createSingleHabit(
            id = FORCE_UPDATE_HABIT_EXCEPTION,
            title = "Updated title",
            body = "Updated body"
        )

        updateHabitUseCase.updateHabit(
            habit = updateHabit,
            stateEvent = HabitDetailStateEvent.UpdateHabitEvent()
        ).collect(object : FlowCollector<DataState<HabitDetailViewState>?> {
            override suspend fun emit(value: DataState<HabitDetailViewState>?) {
                assert(value?.stateMessage?.response?.message?.contains(CacheErrors.CACHE_ERROR_UNKNOWN) ?: false)
            }
        })

        val habitInCache = cacheDataSource.searchHabitById(updateHabit.id)
        assertNotEquals(habitInCache, updateHabit)

        val habitInNetwork = networkDataSource.searchHabit(updateHabit)
        assertNotEquals(habitInNetwork, updateHabit)
    }


}
