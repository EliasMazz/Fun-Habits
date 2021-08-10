package com.yolo.fun_habits.di

import com.codingwithmitch.cleannotes.framework.presentation.end_to_end.HabitFeatureTest
import com.yolo.fun_habits.framework.datasource.cache.HabitDaoServiceImplTest
import com.yolo.fun_habits.framework.datasource.network.HabitFirestoreServiceTest
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

    fun inject(habitFirestoreServiceTest: HabitFirestoreServiceTest)

    fun inject(habitDaoServiceImplTest: HabitDaoServiceImplTest)

    fun inject(habitFeatureTest: HabitFeatureTest)

}
