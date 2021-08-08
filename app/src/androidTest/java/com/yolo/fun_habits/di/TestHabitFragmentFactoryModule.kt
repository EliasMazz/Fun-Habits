package com.yolo.fun_habits.di

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.framework.presentation.TestHabitFragmentFactory
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Singleton

@FlowPreview
@ExperimentalCoroutinesApi
@Module
object TestHabitFragmentFactoryModule {

    @Singleton
    @JvmStatic
    @Provides
    fun provideHabitFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory,
        dateUtil: DateUtil
    ): FragmentFactory {
        return TestHabitFragmentFactory(viewModelFactory, dateUtil)
    }
}
