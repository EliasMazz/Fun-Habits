package com.yolo.fun_habits.dependencyinjection

import com.yolo.fun_habits.framework.presentation.BaseApplication
import com.yolo.fun_habits.framework.presentation.MainActivity
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
