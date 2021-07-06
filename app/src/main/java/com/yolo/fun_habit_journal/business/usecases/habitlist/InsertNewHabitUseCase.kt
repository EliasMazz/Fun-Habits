package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.data.cache.HabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResponseHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.data.network.HabitNetworkDataSouce
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.util.safeApiCall
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.model.HabitFactory
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class InsertNewHabitUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSouce: IHabitNetworkDataSource,
    private val habitFactory: HabitFactory
) {
    fun insertNewHabit(
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

        val cacheResponse = object : CacheResponseHandler<HabitListViewState, Long>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override fun handleSuccess(result: Long): DataState<HabitListViewState> {
                return getCacheInsertHabitResponse(result, newHabit, stateEvent)
            }
        }.getResult()

        emit(cacheResponse)

        updateNetwork(cacheResponse?.stateMessage?.response?.message, newHabit)
    }

    private suspend fun updateNetwork(
        cacheResponse: String?,
        newHabit: Habit
    ) {
        if (cacheResponse.equals(INSERT_HABIT_SUCCESS)) {
            safeApiCall(IO) {
                habitNetworkDataSouce.insertOrUpdateHabit(newHabit)
            }
        }
    }

    private fun getCacheInsertHabitResponse(
        cacheResult: Long,
        newHabit: Habit,
        stateEvent: StateEvent
    ): DataState<HabitListViewState> =
        if (cacheResult > 0) {
            val viewState = HabitListViewState(newHabit = newHabit)
            DataState.data(
                response = Response(
                    message = INSERT_HABIT_SUCCESS,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Success()
                ),
                data = viewState,
                stateEvent = stateEvent
            )
        } else {
            DataState.data(
                response = Response(
                    message = INSERT_HABIT_FAILED,
                    uiComponentType = UIComponentType.Toast(),
                    messageType = MessageType.Error()
                ),
                data = null,
                stateEvent = stateEvent
            )
        }

    companion object {
        const val INSERT_HABIT_SUCCESS = "Successfully inserted new habit"
        const val INSERT_HABIT_FAILED = "Failed to insert new note"
    }
}
