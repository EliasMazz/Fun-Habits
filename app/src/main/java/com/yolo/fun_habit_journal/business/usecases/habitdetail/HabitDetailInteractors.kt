package com.yolo.fun_habit_journal.business.usecases.habitdetail

import com.yolo.fun_habit_journal.business.usecases.common.usecase.DeleteHabitUseCase
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.UpdateHabitUseCase
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailViewState

class HabitDetailInteractors(
    val deleteHabitUseCase: DeleteHabitUseCase<HabitDetailViewState>,
    val updateHabitUseCase: UpdateHabitUseCase
)
