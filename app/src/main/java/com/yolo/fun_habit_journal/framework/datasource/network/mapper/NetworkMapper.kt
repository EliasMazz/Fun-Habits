package com.yolo.fun_habit_journal.framework.datasource.network.mapper

import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.business.domain.util.EntityMapper
import com.yolo.fun_habit_journal.framework.datasource.network.model.HabitNetworkEntity
import javax.inject.Inject

class NetworkMapper
@Inject
constructor(
    private val dateUtil: DateUtil
) : EntityMapper<HabitNetworkEntity, Habit> {
    override fun mapFromEntity(entity: HabitNetworkEntity): Habit {
        return Habit(
            id = entity.id,
            title = entity.title,
            body = entity.body,
            updated_at = dateUtil.convertFirebaseTimestampToStringDate(entity.updated_at),
            created_at = dateUtil.convertFirebaseTimestampToStringDate(entity.created_at)
        )
    }

    override fun mapToEntity(domainModel: Habit): HabitNetworkEntity {
        return HabitNetworkEntity(
            id = domainModel.id,
            title = domainModel.title,
            body = domainModel.body,
            updated_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.updated_at),
            created_at = dateUtil.convertStringDateToFirebaseTimestamp(domainModel.created_at)
        )
    }
}
