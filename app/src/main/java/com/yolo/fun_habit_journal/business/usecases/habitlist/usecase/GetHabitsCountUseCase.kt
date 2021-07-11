package com.yolo.fun_habit_journal.business.usecases.habitlist.usecase

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.domain.state.DataState
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateEvent
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val GET_HABITS_COUNT_SUCCESS = "Successfully retrieved the number of habits from the cache."
const val GET_HABITS_COUNT_FAILED = "Failed to get the number of habits from the cache."

class GetHabitsCountUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource
) {
    fun getHabitsCount(
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.getHabitsCount()
        }

        val dataState = object : CacheResultHandler<HabitListViewState, Int>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: Int): DataState<HabitListViewState> {
                val viewState = HabitListViewState(
                    habitsCountInCache = result
                )
                return DataState.data(
                    response = Response(
                        message = GET_HABITS_COUNT_SUCCESS,
                        uiComponentType = UIComponentType.None,
                        messageType = MessageType.Success
                    ),
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(dataState)
    }
}
