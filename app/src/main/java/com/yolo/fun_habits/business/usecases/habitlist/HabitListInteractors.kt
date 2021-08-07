package com.yolo.fun_habits.business.usecases.habitlist

import com.yolo.fun_habits.business.usecases.habitlist.usecase.GetListHabitstUseCase
import com.yolo.fun_habits.business.usecases.habitlist.usecase.InsertNewHabitUseCase

class HabitListInteractors(
    val insertNewHabitUseCase: InsertNewHabitUseCase,
    val getHabitstListUseCase: GetListHabitstUseCase
)
