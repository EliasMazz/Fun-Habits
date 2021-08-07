package com.yolo.fun_habits.framework.presentation.habitdetail.state

import android.os.Parcelable
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.state.ViewState
import kotlinx.android.parcel.Parcelize

@Parcelize
data class HabitDetailViewState(
    var habit: Habit? = null,
    var isUpdatePending: Boolean? = null

) : Parcelable, ViewState

