package com.yolo.fun_habit_journal.dependencyinjection

import android.app.Application
import com.yolo.fun_habit_journal.framework.presentation.BaseApplication
import com.yolo.fun_habit_journal.framework.presentation.MainActivity
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.HabitDetailFragment
import com.yolo.fun_habit_journal.framework.presentation.habitlist.HabitListFragment
import com.yolo.fun_habit_journal.framework.presentation.splash.SplashFragment
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Singleton
@Component(
    modules = [
        AppModule::class,
        ProductionModule::class,
        HabitViewModelModule::class,
        HabitFragmentFactoryModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: BaseApplication): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}
