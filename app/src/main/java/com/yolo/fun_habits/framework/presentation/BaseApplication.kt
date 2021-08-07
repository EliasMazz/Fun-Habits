package com.yolo.fun_habits.framework.presentation

import androidx.multidex.MultiDexApplication
import com.yolo.fun_habits.dependencyinjection.AppComponent
import com.yolo.fun_habits.dependencyinjection.DaggerAppComponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
open class BaseApplication : MultiDexApplication() {

    lateinit var appComponent: AppComponent

    override fun onCreate() {
        super.onCreate()
        initAppComponent()
    }

    open fun initAppComponent() {
        appComponent = DaggerAppComponent.factory()
            .create(this)
    }
}
