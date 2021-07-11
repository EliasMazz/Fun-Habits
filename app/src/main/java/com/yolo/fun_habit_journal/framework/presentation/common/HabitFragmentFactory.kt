package com.yolo.fun_habit_journal.framework.presentation.common

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.HabitDetailFragment
import com.yolo.fun_habit_journal.framework.presentation.habitlist.HabitListFragment
import com.yolo.fun_habit_journal.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class HabitFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): FragmentFactory(){

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when(className){

            HabitListFragment::class.java.name -> {
                val fragment =  HabitListFragment(viewModelFactory, dateUtil)
                fragment
            }

            HabitDetailFragment::class.java.name -> {
                val fragment = HabitDetailFragment(viewModelFactory)
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                fragment
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }
}
