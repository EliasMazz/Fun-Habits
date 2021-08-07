package com.yolo.fun_habits

import androidx.test.core.app.ApplicationProvider
import com.yolo.fun_habits.framework.presentation.TestBaseApplication
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

abstract class BaseTest {

    @ExperimentalCoroutinesApi
    @FlowPreview
    val application: TestBaseApplication = ApplicationProvider.getApplicationContext() as TestBaseApplication

    abstract fun injectTest()
}
