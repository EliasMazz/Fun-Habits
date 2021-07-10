package com.yolo.fun_habit_journal.business.usecases.appstart

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.util.NetworkResultHandler
import com.yolo.fun_habit_journal.business.data.network.util.safeNetworkCall
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import kotlinx.coroutines.Dispatchers.IO

class SyncDeletedHabitsUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource
) {
    suspend fun syncDeletedHabits() {
        val networkResult = safeNetworkCall(IO) {
            habitNetworkDataSource.getDeletedHabitList()
        }

        val dataState = object : NetworkResultHandler<List<Habit>, List<Habit>>(
            response = networkResult,
            stateEvent = null
        ) {
            override suspend fun handleSuccess(result: List<Habit>): DataState<List<Habit>> {
                return DataState.data(
                    response = null,
                    data = result,
                    stateEvent = null
                )
            }
        }.getResult()

        val deletedNetworkHabitList = dataState.data ?: ArrayList()

        safeCacheCall(IO) {
            habitCacheDataSource.deleteHabits(deletedNetworkHabitList)
        }
    }
}
