package com.yolo.fun_habit_journal.framework.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.HabitViewModelFactory

class SplashFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseFragment(R.layout.fragment_splash) {

    val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun inject() {
        TODO("Not yet implemented")
    }
}
