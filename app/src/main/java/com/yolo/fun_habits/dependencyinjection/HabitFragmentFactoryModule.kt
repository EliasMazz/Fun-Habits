package com.yolo.fun_habits.dependencyinjection

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.framework.presentation.common.HabitFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Module
object HabitFragmentFactoryModule {

    @JvmStatic
    @Singleton
    @Provides
    fun provideHabitFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): FragmentFactory {
        return HabitFragmentFactory(
            viewModelFactory,
            dateUtil
        )
    }
}
