package com.yolo.fun_habits.business.usecases.habitdetail

import com.yolo.fun_habits.business.usecases.habitdetail.usecase.DeleteHabitUseCase
import com.yolo.fun_habits.business.usecases.habitdetail.usecase.UpdateHabitUseCase

class HabitDetailInteractors(
    val deleteHabitUseCase: DeleteHabitUseCase,
    val updateHabitUseCase: UpdateHabitUseCase
)
