package com.yolo.fun_habits.business.usecases.habitdetail.usecase

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habits.business.data.cache.util.safeCacheCall
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.data.network.util.safeNetworkCall
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

const val DELETE_HABIT_SUCCESS = "Succesfully deleted the habit"
const val DELETE_HABIT_FAILURE = "Failed to delete the habit"
const val DELETE_HABIT_PENDING = "Delete pending..."
const val DELETE_ARE_YOU_SURE = "Are you sure you want to delete this?"

class DeleteHabitUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSouce: IHabitNetworkDataSource
) {
    fun invoke(
        habit: Habit,
        stateEvent: StateEvent
    ): Flow<DataState<HabitDetailViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.deleteHabit(habit.id)
        }

        val dataState = object : CacheResultHandler<HabitDetailViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: Int): DataState<HabitDetailViewState> {
                return handleCacheSuccess(stateEvent, result)
            }
        }.getResult()

        emit(dataState)

        updateNetwork(
            message = dataState?.stateMessage?.response?.message,
            habit = habit
        )
    }

    private suspend fun updateNetwork(message: String?, habit: Habit) {
        if (message == DELETE_HABIT_SUCCESS) {
            safeNetworkCall(IO) {
                habitNetworkDataSouce.deleteHabit(habit.id)
            }

            safeNetworkCall(IO) {
                habitNetworkDataSouce.insertDeletedHabit(habit)
            }
        }
    }

    private fun handleCacheSuccess(stateEvent: StateEvent, result: Int): DataState<HabitDetailViewState> =
        if (result > 0) {
            DataState.data(
                response = Response(
                    message = DELETE_HABIT_SUCCESS,
                    uiComponentType = UIComponentType.None,
                    messageType = MessageType.Success
                ),
                data = null,
                stateEvent = stateEvent
            )
        } else {
            DataState.data(
                response = Response(
                    message = DELETE_HABIT_FAILURE,
                    uiComponentType = UIComponentType.Toast,
                    messageType = MessageType.Error
                ),
                data = null,
                stateEvent = stateEvent
            )
        }
}
