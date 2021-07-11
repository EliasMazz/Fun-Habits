package com.yolo.fun_habit_journal.business.usecases.appstart.usecase

import com.yolo.fun_habit_journal.business.data.cache.abstraction.IHabitCacheDataSource
import com.yolo.fun_habit_journal.business.data.cache.util.CacheResultHandler
import com.yolo.fun_habit_journal.business.data.cache.util.safeCacheCall
import com.yolo.fun_habit_journal.business.data.network.abstraction.IHabitNetworkDataSource
import com.yolo.fun_habit_journal.business.data.network.util.NetworkResultHandler
import com.yolo.fun_habit_journal.business.data.network.util.safeNetworkCall
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DataState
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/*
    Query all habits in the cache, It will then search on the network for
    each corresponding habit but with an extra filter. It will only return habits where
    cached_habit.updated_at < network_habit.updated_at. It will update the cached habit
    where that condition is met. If the habit does not exist in the network (maybe due to
    network being down at time of insertion), insert it, this is done after checking for deleted
    habits and  performing that sync.
 */

class SyncHabitsUseCase(
    private val habitCacheDataSource: IHabitCacheDataSource,
    private val habitNetworkDataSource: IHabitNetworkDataSource
) {
    suspend fun syncHabits() {
        val cachedHabitList = getCachedHabits()
        syncNetworkHabitsWithCachedHabits(ArrayList(cachedHabitList))
    }

    private suspend fun getCachedHabits(): List<Habit> {
        val cacheResult = safeCacheCall(IO) {
            habitCacheDataSource.getAllHabits()
        }

        val dataState = object : CacheResultHandler<List<Habit>, List<Habit>>(
            response = cacheResult,
            stateEvent = null
        ) {
            override suspend fun handleDataState(result: List<Habit>): DataState<List<Habit>> {
                return DataState.data(
                    response = null,
                    data = result,
                    stateEvent = null
                )
            }
        }.getResult()

        return dataState?.data ?: ArrayList()
    }

    private suspend fun syncNetworkHabitsWithCachedHabits(
        cacheHabitsInMemory: ArrayList<Habit>
    ) = withContext(IO) {

        val networkResult = safeNetworkCall(IO) {
            habitNetworkDataSource.getAllHabits()
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

        val habitListInNetwork = dataState.data ?: ArrayList()

        val job = launch {
            for (habitInNetwork in habitListInNetwork) {
                habitCacheDataSource.searchHabitById(habitInNetwork.id)?.let { cachedHabit ->
                    cacheHabitsInMemory.remove(habitInNetwork)
                    checkIfCachedHabitRequiresUpdate(cachedHabit, habitInNetwork)
                } ?: habitCacheDataSource.insertHabit(habitInNetwork)

            }
        }
        job.join()

        for (cachedHabit in cacheHabitsInMemory) {
            safeNetworkCall(IO) {
                habitNetworkDataSource.insertOrUpdateHabit(cachedHabit)
            }
        }
    }

    private suspend fun checkIfCachedHabitRequiresUpdate(
        cachedHabit: Habit,
        networkHabit: Habit
    ) {
        val cacheUpdatedAt = cachedHabit.updated_at
        val networkUpdatedAt = networkHabit.updated_at

        if (networkUpdatedAt > cacheUpdatedAt) {
            safeCacheCall(IO) {
                habitCacheDataSource.updateHabit(
                    id = networkHabit.id,
                    title = networkHabit.title,
                    body = networkHabit.body,
                    timestamp = networkHabit.updated_at
                )
            }
        } else {
            safeNetworkCall(IO) {
                habitNetworkDataSource.insertOrUpdateHabit(cachedHabit)
            }
        }
    }
}
