package com.yolo.fun_habit_journal.business.usecases.habitlist

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

const val SEARCH_HABITS_SUCCESS = "Successfully retrieved list of habits"
const val SEARCH_HABITS_NO_MATCHING_RESULTS = "There are no habits that match that query"
const val SEARCH_HABITS_FAILED = "Failed to retrieve the list of habits"

class SearchHabitsUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource
) {
    fun searchHabits(
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<HabitListViewState>?> = flow {
        var updatedPage = page
        if (page <= 0) {
            updatedPage = 1
        }

        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.searchHabits(
                query = query,
                filterAndOrder = filterAndOrder,
                page = updatedPage
            )
        }

        val dataState = object : CacheResultHandler<HabitListViewState, List<Habit>>(
            response = cacheResult,
            stateEvent = stateEvent
        ) {
            override suspend fun handleDataState(result: List<Habit>): DataState<HabitListViewState> {
                var message: String? = SEARCH_HABITS_SUCCESS
                var uiComponentType: UIComponentType = UIComponentType.None

                if (result.isEmpty()) {
                    message = SEARCH_HABITS_NO_MATCHING_RESULTS
                    uiComponentType = UIComponentType.Toast
                }
                return DataState.data(
                    response = Response(
                        message = message,
                        uiComponentType = uiComponentType,
                        messageType = MessageType.Success
                    ), data = HabitListViewState(
                        habitList = ArrayList(result)
                    ), stateEvent = stateEvent
                )
            }
        }.getResult()

        emit(dataState)
    }
}
