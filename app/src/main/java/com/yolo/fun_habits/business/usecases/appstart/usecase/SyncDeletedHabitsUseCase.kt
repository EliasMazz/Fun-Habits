package com.yolo.fun_habits.business.usecases.appstart.usecase

import com.yolo.fun_habits.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habits.business.data.cache.util.safeCacheCall
import com.yolo.fun_habits.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habits.business.data.network.util.NetworkResultHandler
import com.yolo.fun_habits.business.data.network.util.safeNetworkCall
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.state.DataState
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
