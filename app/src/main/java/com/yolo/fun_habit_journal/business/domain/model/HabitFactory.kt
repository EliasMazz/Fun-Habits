package com.yolo.fun_habit_journal.business.domain.model

import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HabitFactory
@Inject
constructor(
    private val dateUtil: DateUtil
) {
    fun createSingleNote(
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
}
