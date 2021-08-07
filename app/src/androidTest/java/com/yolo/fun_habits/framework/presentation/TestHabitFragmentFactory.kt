package com.yolo.fun_habits.framework.presentation

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.framework.presentation.habitdetail.HabitDetailFragment
import com.yolo.fun_habits.framework.presentation.habitlist.HabitListFragment
import com.yolo.fun_habits.framework.presentation.splash.SplashFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
class TestHabitFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
): FragmentFactory(){

    lateinit var uiController: UIController

    override fun instantiate(classLoader: ClassLoader, className: String) =

        when(className){

            HabitListFragment::class.java.name -> {
                val fragment = HabitListFragment(viewModelFactory, dateUtil)
                if(::uiController.isInitialized){
                    fragment.setUIController(uiController)
                }
                fragment
            }

            HabitDetailFragment::class.java.name -> {
                val fragment = HabitDetailFragment(viewModelFactory)
                if(::uiController.isInitialized){
                    fragment.setUIController(uiController)
                }
                fragment
            }

            SplashFragment::class.java.name -> {
                val fragment = SplashFragment(viewModelFactory)
                if(::uiController.isInitialized){
                    fragment.setUIController(uiController)
                }
                fragment
            }

            else -> {
                super.instantiate(classLoader, className)
            }
        }

}
