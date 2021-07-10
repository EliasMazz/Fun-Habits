package com.yolo.fun_habit_journal.framework.datasource.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.yolo.fun_habit_journal.framework.datasource.cache.model.HabitCacheEntity

const val HABIT_ORDER_ASC: String = ""
const val HABIT_ORDER_DESC: String = "-"
const val HABIT_FILTER_TITLE = "title"
const val HABIT_FILTER_DATE_CREATED = "created_at"

const val ORDER_BY_ASC_DATE_UPDATED = HABIT_ORDER_ASC + HABIT_FILTER_DATE_CREATED
const val ORDER_BY_DESC_DATE_UPDATED = HABIT_ORDER_DESC + HABIT_FILTER_DATE_CREATED
const val ORDER_BY_ASC_TITLE = HABIT_ORDER_ASC + HABIT_FILTER_TITLE
const val ORDER_BY_DESC_TITLE = HABIT_ORDER_DESC + HABIT_FILTER_TITLE

const val HABIT_PAGINATION_PAGE_SIZE = 30

@Dao
interface HabitDao {

    @Insert
    suspend fun insertHabit(habit: HabitCacheEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertHabitList(habitList: List<HabitCacheEntity>): LongArray

    @Query("SELECT * FROM habits WHERE id = :id")
    suspend fun searchHabitById(id: String): HabitCacheEntity?

    @Query("DELETE FROM habits WHERE id IN (:ids)")
    suspend fun deleteHabitList(ids: List<String>): Int

    @Query("DELETE FROM habits")
    suspend fun deleteAllHabits()

    @Query(
        """
        UPDATE habits 
        SET 
        title = :title, 
        body = :body,
        updated_at = :updated_at
        WHERE id = :primaryKey
        """
    )
    suspend fun updateHabit(
        primaryKey: String,
        title: String,
        body: String?,
        updated_at: String
    ): Int

    @Query("DELETE FROM habits WHERE id = :primaryKey")
    suspend fun deleteHabit(primaryKey: String): Int

    @Query("SELECT * FROM habits")
    suspend fun getAllHabits(): List<HabitCacheEntity>

    @Query(
        """
        SELECT * FROM habits 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY updated_at DESC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchHabitsOrderByDateDESC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<HabitCacheEntity>

    @Query(
        """
        SELECT * FROM habits 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY updated_at ASC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchHabitsOrderByDateASC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<HabitCacheEntity>

    @Query(
        """
        SELECT * FROM habits 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY title DESC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchHabitsOrderByTitleDESC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<HabitCacheEntity>

    @Query(
        """
        SELECT * FROM habits 
        WHERE title LIKE '%' || :query || '%' 
        OR body LIKE '%' || :query || '%' 
        ORDER BY title ASC LIMIT (:page * :pageSize)
        """
    )
    suspend fun searchHabitsOrderByTitleASC(
        query: String,
        page: Int,
        pageSize: Int = HABIT_PAGINATION_PAGE_SIZE
    ): List<HabitCacheEntity>


    @Query("SELECT COUNT(*) FROM habits")
    suspend fun getHabitsCount(): Int
}


suspend fun HabitDao.returnOrderedQuery(
    query: String,
    filterAndOrder: String,
    page: Int
): List<HabitCacheEntity> {

    when {
        filterAndOrder.contains(ORDER_BY_DESC_DATE_UPDATED) -> {
            return searchHabitsOrderByDateDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_DATE_UPDATED) -> {
            return searchHabitsOrderByDateASC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_DESC_TITLE) -> {
            return searchHabitsOrderByTitleDESC(
                query = query,
                page = page
            )
        }

        filterAndOrder.contains(ORDER_BY_ASC_TITLE) -> {
            return searchHabitsOrderByTitleASC(
                query = query,
                page = page
            )
        }
        else ->
            return searchHabitsOrderByDateDESC(
                query = query,
                page = page
            )
    }
}
