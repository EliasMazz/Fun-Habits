package com.yolo.fun_habits.framework.presentation.habitlist.state

import android.os.Parcelable
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HabitListViewState(
    var habitList: ArrayList<Habit>? = null,
    var newHabit: Habit? = null,
    var layoutManagerState: Parcelable? = null

) : Parcelable, ViewState {

    @Parcelize
    data class HabitPendingDelete(
        var habit: Habit? = null,
        var listPosition: Int? = null
    ) : Parcelable
}
