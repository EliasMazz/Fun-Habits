package com.yolo.fun_habit_journal.business.usecases.habitlist

import com.yolo.fun_habit_journal.business.usecases.common.usecase.DeleteHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.DeleteMultipleHabitsUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.GetHabitsCountUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.InsertNewHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.RestoreDeletedHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.SearchHabitsUseCase
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState

class HabitListInteractors(
    insertNewHabitUseCase: InsertNewHabitUseCase,
    deleteHabitUseCase: DeleteHabitUseCase<HabitListViewState>,
    searchHabitsUseCase: SearchHabitsUseCase,
    getHabitsCountUseCase: GetHabitsCountUseCase,
    restoreDeletedHabitUseCase: RestoreDeletedHabitUseCase,
    deleteMultipleHabitsUseCase: DeleteMultipleHabitsUseCase
)
