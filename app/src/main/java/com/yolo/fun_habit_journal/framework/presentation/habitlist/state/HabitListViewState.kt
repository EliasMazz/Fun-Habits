package com.yolo.fun_habit_journal.framework.presentation.habitlist.state

import android.os.Parcelable
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HabitListViewState(
    var habitList: ArrayList<Habit>? = null,
    var newHabit: Habit? = null,
    var habitPendingDelete: HabitPendingDelete? = null,
    var searchQuery: String? = null,
    var page: Int? = null,
    var isQueryExhausted: Boolean? = null,
    var filter: String? = null,
    var order: String? = null,
    var layoutManagerState: Parcelable? = null,
    var numNotesInCache: Int? = null

) : Parcelable, ViewState {

    @Parcelize
    data class HabitPendingDelete(
        var habit: Habit? = null,
        var listPosition: Int? = null
    ) : Parcelable
}
