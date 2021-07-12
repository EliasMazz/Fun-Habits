package com.yolo.fun_habit_journal.business.usecases.habitlist.usecase

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
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

const val GET_LIST_HABITS_SUCCESS = "Successfully retrieved list habits from the cache."
const val GET_LIST_HABITS_FAILED = "Failed to get the list of habits from the cache."
const val GET_LIST_HABITS_NO_MATCHING_RESULTS = "There are no habits in the list"

class GetListHabitstUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource
) {
    fun invoke(
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.getAllHabits()
        }

        val dataState = object : CacheResultHandler<HabitListViewState, List<Habit>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: List<Habit>): DataState<HabitListViewState> {
                var message: String? = GET_LIST_HABITS_SUCCESS
                var uiComponentType: UIComponentType? = UIComponentType.None

                if (result.isEmpty()) {
                    message = GET_LIST_HABITS_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast
                }

                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType as UIComponentType,
                        messageType = MessageType.Success
                    ),
                    data = HabitListViewState(
                        habitList = ArrayList(result)
                    ),
                    stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(dataState)
    }
}
