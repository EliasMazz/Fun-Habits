package com.yolo.fun_habits.dependencyinjection

import com.yolo.fun_habits.business.TempTest
import com.yolo.fun_habits.framework.presentation.TestBaseApplication
import dagger.BindsInstance
import dagger.Component
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@FlowPreview
@Singleton
@Component(
    modules = [
        TestModule::class,
        AppModule::class,
        TestHabitFragmentFactoryModule::class,
        HabitViewModelModule::class
    ]
)
interface TestAppComponent : AppComponent {

    @Component.Factory
    interface Factory {
        fun create(@BindsInstance app: TestBaseApplication): TestAppComponent
    }

    fun inject(tempTest: TempTest)

}
