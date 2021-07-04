package com.yolo.fun_habit_journal.business.domain.util

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DateUtil
@Inject
constructor(
    private val dateFormat: SimpleDateFormat
) {
    fun removeTimeFromDateString(dataString: String): String {
        return dataString.substring(0, dataString.indexOf(" "))
    }

    fun convertFirebaseTimestampToStringDate(timestamp: Timestamp): String {
        return dateFormat.format(timestamp.toDate())
    }

    fun convertStringDateToFirebaseTimestamp(date: String): Timestamp{
        return Timestamp(dateFormat.parse(date))
    }

    fun getCurrentTimestamp(): String {
        return dateFormat.format(Date())
    }
}
