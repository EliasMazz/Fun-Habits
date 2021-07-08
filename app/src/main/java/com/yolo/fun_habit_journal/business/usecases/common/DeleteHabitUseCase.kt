package com.yolo.fun_habit_journal.business.usecases.common

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.util.safeApiCall
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val DELETE_HABIT_SUCCESS = "Succesfully deleted the habit"
const val DELETE_HABIT_FAILURE = "Failed to delete the habit"

class DeleteHabitUseCase<ViewState>(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSouce: IHabitNetworkDataSource
) {
    fun deleteHabit(
        habit: Habit,
        stateEvent: StateEvent
    ): Flow<DataState<ViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.deleteHabit(habit.id)
        }

        val cacheResultHandler = object : CacheResultHandler<ViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleDataState(result: Int): DataState<ViewState> {
                return handleCacheSuccess(stateEvent, result)
            }
        }.getResult()

        emit(cacheResultHandler)

        updateNetwork(
            message = cacheResultHandler?.stateMessage?.response?.message,
            habit = habit
        )
    }

    private suspend fun updateNetwork(message: String?, habit: Habit) {
        if (message == DELETE_HABIT_SUCCESS) {
            safeApiCall(IO) {
                habitNetworkDataSouce.deleteHabit(habit.id)
            }

            safeApiCall(IO) {
                habitNetworkDataSouce.insertDeletedHabit(habit)
            }
        }
    }

    private fun handleCacheSuccess(stateEvent: StateEvent, result: Int): DataState<ViewState> =
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
