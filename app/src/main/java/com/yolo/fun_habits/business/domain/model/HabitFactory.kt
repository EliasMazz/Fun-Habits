package com.yolo.fun_habits.business.domain.model

import com.yolo.fun_habits.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitFactory
@Inject
constructor(
    private val dateUtil: DateUtil
) {
    fun createSingleHabit(
        id: String? = null,
        title: String,
        body: String? = null
    ): Habit = Habit(
        id = id ?: UUID.randomUUID().toString(),
        title = title,
        body = body ?: "",
        created_at = dateUtil.getCurrentTimestamp(),
        updated_at = dateUtil.getCurrentTimestamp()
    )

    fun createHabitList(numHabits: Int): List<Habit> {
        val list: ArrayList<Habit> = ArrayList()
        for (i in 0 until numHabits) {
            list.add(
                createSingleHabit(
                    title = UUID.randomUUID().toString(),
                    body = UUID.randomUUID().toString()
                )
            )
        }
        return list
    }

}
