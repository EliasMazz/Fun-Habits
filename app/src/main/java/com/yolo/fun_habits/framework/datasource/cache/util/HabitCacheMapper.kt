package com.yolo.fun_habits.framework.datasource.cache.util

import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.business.domain.util.EntityMapper
import com.yolo.fun_habits.framework.datasource.cache.model.HabitCacheEntity
import javax.inject.Inject

class HabitCacheMapper @Inject constructor(
    private val dateUtil: DateUtil
) : EntityMapper<HabitCacheEntity, Habit> {
    override fun mapFromEntity(entity: HabitCacheEntity): Habit {
        return Habit(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            created_at = entity.created_at,
            updated_at = entity.updated_at
        )
    }

    override fun mapToEntity(domainModel: Habit): HabitCacheEntity {
        return HabitCacheEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            created_at = domainModel.created_at,
            updated_at = domainModel.updated_at
        )
    }
}
