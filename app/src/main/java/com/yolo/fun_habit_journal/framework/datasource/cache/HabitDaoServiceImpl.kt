package com.yolo.fun_habit_journal.framework.datasource.cache

import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.datasource.cache.abstraction.IHabitDaoService
import com.yolo.fun_habit_journal.framework.datasource.cache.util.HabitCacheMapper
import com.yolo.fun_habit_journal.framework.datasource.database.HabitDao
import com.yolo.fun_habit_journal.framework.datasource.database.returnOrderedQuery
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitDaoServiceImpl
@Inject
constructor(
    private val habitDao: HabitDao,
    private val habitMapperHabit: HabitCacheMapper,
    private val dateUtil: DateUtil
) : IHabitDaoService {

    override suspend fun insertHabit(habit: Habit): Long =
        habitDao.insertHabit(habitMapperHabit.mapToEntity(habit))

    override suspend fun insertHabitList(habitList: List<Habit>): LongArray =
        habitDao.insertHabitList(
            habitList = habitList.map { habitMapperHabit.mapToEntity(it) }
        )

    override suspend fun searchHabitById(id: String): Habit? =
        habitDao.searchHabitById(id)?.let {
            habitMapperHabit.mapFromEntity(it)
        }

    override suspend fun updateHabit(
        id: String,
        title: String,
        body: String?,
        timestamp: String?
    ): Int =
        if (timestamp != null) {
            habitDao.updateHabit(
                primaryKey = id,
                title = title,
                body = body,
                updated_at = timestamp
            )
        } else {
            habitDao.updateHabit(
                primaryKey = id,
                title = title,
                body = body,
                updated_at = dateUtil.getCurrentTimestamp()
            )
        }

    override suspend fun deleteHabit(id: String): Int =
        habitDao.deleteHabit(id)


    override suspend fun deleteHabitList(habitList: List<Habit>): Int {
        val habitListIds = habitList.map { it.id }
        return habitDao.deleteHabitList(habitListIds)
    }

    override suspend fun getAllHabits(): List<Habit> =
        habitDao.getAllHabits().map { habitMapperHabit.mapFromEntity(it) }

    override suspend fun searchHabitsOrderByDateDESC(query: String, page: Int, pageSize: Int): List<Habit> =
        habitDao.searchHabitsOrderByDateDESC(
            query = query,
            page = page,
            pageSize = pageSize
        ).map { habitMapperHabit.mapFromEntity(it) }

    override suspend fun searchHabitsOrderByDateASC(query: String, page: Int, pageSize: Int): List<Habit> =
        habitDao.searchHabitsOrderByDateASC(
            query = query,
            page = page,
            pageSize = pageSize
        ).map { habitMapperHabit.mapFromEntity(it) }

    override suspend fun searchHabitsOrderByTitleDESC(query: String, page: Int, pageSize: Int): List<Habit> =
        habitDao.searchHabitsOrderByTitleDESC(
            query = query,
            page = page,
            pageSize = pageSize
        ).map { habitMapperHabit.mapFromEntity(it) }


    override suspend fun searchHabitsOrderByTitleASC(query: String, page: Int, pageSize: Int): List<Habit> =
        habitDao.searchHabitsOrderByTitleASC(
            query = query,
            page = page,
            pageSize = pageSize
        ).map { habitMapperHabit.mapFromEntity(it) }

    override suspend fun getHabitsCount(): Int = habitDao.getHabitsCount()

    override suspend fun returnOrderedQuery(query: String, filterAndOrder: String, page: Int): List<Habit> =
        habitDao.returnOrderedQuery(
            query = query,
            page = page,
            filterAndOrder = filterAndOrder
        ).map { habitMapperHabit.mapFromEntity(it) }
}