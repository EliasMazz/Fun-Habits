package com.yolo.fun_habit_journal.business.usecases.habitlist.usecase

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.util.safeNetworkCall
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val RESTORE_HABIT_SUCCESS = "Successfully restored the deleted habit"
const val RESTORE_HABIT_FAILED = "Failed to restore the deleted habit"

class RestoreDeletedHabitUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource
) {
    fun restoreDeletedHabit(
        habit: Habit,
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.insertHabit(habit)
        }

        val dataState = object : CacheResultHandler<HabitListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: Long): DataState<HabitListViewState> {
                return if (result > 0) {
                    getDataStateSuccess(habit, stateEvent)
                } else {
                    getDataStateFailure(stateEvent)
                }
            }
        }.getResult()

        emit(dataState)

        updateNetwork(dataState?.stateMessage?.response?.message, habit)
    }

    private suspend fun updateNetwork(message: String?, habit: Habit) {
        if (message == RESTORE_HABIT_SUCCESS) {
            safeNetworkCall(IO) {
                habitNetworkDataSource.insertOrUpdateHabit(habit)
            }

            safeNetworkCall(IO) {
                habitNetworkDataSource.deleteDeletedHabit(habit)
            }
        }
    }

    private fun getDataStateSuccess(habit: Habit, stateEvent: StateEvent): DataState<HabitListViewState> {
        val viewState = HabitListViewState(
            habitPendingDelete = HabitListViewState.HabitPendingDelete(
                habit = habit
            )
        )
        return DataState.data(
            response = Response(
                message = RESTORE_HABIT_SUCCESS,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Success
            ), data = viewState,
            stateEvent = stateEvent
        )
    }

    private fun getDataStateFailure(stateEvent: StateEvent): DataState<HabitListViewState> =
        DataState.data(
            response = Response(
                message = RESTORE_HABIT_FAILED,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Success
            ), data = null,
            stateEvent = stateEvent
        )

}
