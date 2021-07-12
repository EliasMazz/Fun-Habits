package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GetListHabitstUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.InsertNewHabitUseCase
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState

class HabitListInteractors(
    val insertNewHabitUseCase: InsertNewHabitUseCase,
    val getHabitstListUseCase: GetListHabitstUseCase
)
