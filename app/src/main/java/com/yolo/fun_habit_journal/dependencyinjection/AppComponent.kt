package com.yolo.fun_habit_journal.dependencyinjection

import android.app.Application
import com.yolo.fun_habit_journal.framework.presentation.MainActivity
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
        ProductionModule::class
    ]
)
interface AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance application: Application): AppComponent
    }

    fun inject(mainActivity: MainActivity)
}
