package com.yolo.fun_habit_journal.framework.presentation.habitdetail.state

import android.os.Parcelable
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HabitDetailViewState(
    var habit: Habit? = null,
    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState

