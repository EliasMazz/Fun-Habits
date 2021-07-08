package com.yolo.fun_habit_journal.business.usecases.habitlist

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
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val DELETE_HABITS_SUCCESS = "Successfully deleted habits"
const val DELETE_HABITS_FAILURE = "Not all the habits you selected were deleted. There was some errors."
const val DELETE_HABITS_YOU_MUST_SELECT = "You haven't selected any habits to delete."
const val DELETE_HABITS_ARE_YOU_SURE = "Are you sure you want to delete these?"

class DeleteMultipleHabitsUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource
) {
    private var onDeleteError: Boolean = false

    fun deleteHabits(
        habitList: List<Habit>,
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {

        val succesfulDeletes: ArrayList<Habit> = ArrayList()
        for (habit in habitList) {
            val cacheResult = safeCacheCall(IO) {
                habitCacheDataSource.deleteHabit(habit.id)
            }

            val dataState = object : CacheResultHandler<HabitListViewState, Int>(
                response = cacheResult,
                stateEvent = stateEvent
            ) {
                override fun handleDataState(result: Int): DataState<HabitListViewState>? {
                    if (result < 0) {
                        onDeleteError = true
                    } else {
                        succesfulDeletes.add(habit)
                    }
                    return null
                }
            }.getResult()

            if (dataState?.stateMessage?.response?.message?.contains(stateEvent.errorInfo()) == true) {
                onDeleteError = true
            }
        }

        if (onDeleteError) {
            emit(getDataStateFailure(stateEvent))
        } else {
            emit(getDataStateSuccess(stateEvent))
        }

        updateNetowrk(succesfulDeletes)
    }

    private suspend fun updateNetowrk(succesfulDeletes: ArrayList<Habit>) {
        for (habit in succesfulDeletes) {
            safeApiCall(IO) {
                habitNetworkDataSource.deleteHabit(habit.id)
            }

            safeApiCall(IO) {
                habitNetworkDataSource.insertDeletedHabit(habit)
            }
        }
    }

    private fun getDataStateFailure(stateEvent: StateEvent) =
        DataState.data<HabitListViewState>(
            response = Response(
                message = DELETE_HABITS_FAILURE,
                uiComponentType = UIComponentType.Dialog,
                messageType = MessageType.Success
            ), data = null,
            stateEvent = stateEvent
        )

    private fun getDataStateSuccess(stateEvent: StateEvent) =
        DataState.data<HabitListViewState>(
            response = Response(
                message = DELETE_HABITS_SUCCESS,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Success
            ), data = null,
            stateEvent = stateEvent
        )
}
