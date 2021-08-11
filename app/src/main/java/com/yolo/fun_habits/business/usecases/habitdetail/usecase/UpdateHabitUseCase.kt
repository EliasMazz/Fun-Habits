package com.yolo.fun_habits.business.usecases.habitdetail.usecase

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habits.business.data.cache.util.safeCacheCall
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.domain.state.UIComponentType
import com.yolo.fun_habits.framework.presentation.habitdetail.state.HabitDetailViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val UPDATE_HABIT_SUCCESS = "Successfully updated habit"
const val UPDATE_HABIT_FAILED = "Failed to update habit"

class UpdateHabitUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource
) {
    fun invoke(
        habit: Habit,
        stateEvent: StateEvent
    ): Flow<DataState<HabitDetailViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.updateHabit(
                id = habit.id,
                title = habit.title,
                body = habit.body,
                timestamp = null
            )
        }

        val dataState = object : CacheResultHandler<HabitDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: Int): DataState<HabitDetailViewState> {
                return if (result > 0) {
                    getDataStateSuccess(stateEvent)
                } else {
                    getDataStateFailure(stateEvent)
                }
            }
        }.getResult()

        emit(dataState)

        updateNetowrk(dataState?.stateMessage?.response?.message, habit)
    }

    private suspend fun updateNetowrk(message: String?, habit: Habit) {
        if (message == UPDATE_HABIT_SUCCESS) {
            safeCacheCall(IO) {
                habitNetworkDataSource.insertOrUpdateHabit(habit)
            }
        }
    }

    private fun getDataStateSuccess(stateEvent: StateEvent): DataState<HabitDetailViewState> =
        DataState.data(
            response = Response(
                message = UPDATE_HABIT_SUCCESS,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Success
            ),
            data = null,
            stateEvent = stateEvent
        )

    private fun getDataStateFailure(stateEvent: StateEvent): DataState<HabitDetailViewState> =
        DataState.data(
            response = Response(
                message = UPDATE_HABIT_FAILED,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Error
            ), data = null,
            stateEvent = stateEvent
        )
}


