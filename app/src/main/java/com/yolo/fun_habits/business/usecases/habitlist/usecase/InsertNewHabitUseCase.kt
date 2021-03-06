package com.yolo.fun_habits.business.usecases.habitlist.usecase

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habits.business.data.cache.util.safeCacheCall
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.data.network.util.safeNetworkCall
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.model.HabitFactory
import com.yolo.fun_habits.business.domain.state.DataState
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateEvent
import com.yolo.fun_habits.business.domain.state.UIComponentType
import com.yolo.fun_habits.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val INSERT_HABIT_SUCCESS = "Successfully inserted new habit"
const val INSERT_HABIT_FAILED = "Failed to insert new habit"

class InsertNewHabitUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource,
    private val habitFactory: HabitFactory
) {
    fun invoke(
        id: String? = null,
        title: String,
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {
        val newHabit = habitFactory.createSingleHabit(
            id = id,
            title = title
        )

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.insertHabit(newHabit)
        }

        val dataState = object : CacheResultHandler<HabitListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: Long): DataState<HabitListViewState> {
                return if (result > 0) {
                    getDataStateSuccess(newHabit, stateEvent)
                } else {
                    getDataStateError(stateEvent)
                }
            }
        }.getResult()

        emit(dataState)

        updateNetwork(dataState?.stateMessage?.response?.message, newHabit)
    }

    private suspend fun updateNetwork(
        message: String?,
        newHabit: Habit
    ) {
        if (message.equals(INSERT_HABIT_SUCCESS)) {
            safeNetworkCall(IO) {
                habitNetworkDataSource.insertOrUpdateHabit(newHabit)
            }
        }
    }

    private fun getDataStateSuccess(
        newHabit: Habit,
        stateEvent: StateEvent
    ): DataState<HabitListViewState> {
        val viewState = HabitListViewState(newHabit = newHabit)
        return DataState.data(
            response = Response(
                message = INSERT_HABIT_SUCCESS,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Success
            ),
            data = viewState,
            stateEvent = stateEvent
        )
    }

    private fun getDataStateError(
        stateEvent: StateEvent
    ): DataState<HabitListViewState> =
        DataState.data(
            response = Response(
                message = INSERT_HABIT_FAILED,
                uiComponentType = UIComponentType.Toast,
                messageType = MessageType.Error
            ),
            data = null,
            stateEvent = stateEvent
        )
}
