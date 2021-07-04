package com.yolo.fun_habit_journal.business.domain.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Habit(
    val id: String,
    val title: String,
    val body: String,
    val updated_at: String,
    val created_at: String
) : Parcelable
