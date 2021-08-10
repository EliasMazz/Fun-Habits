package com.yolo.fun_habits.util

import androidx.test.espresso.idling.CountingIdlingResource
import com.yolo.fun_habits.framework.util.printLogD


object EspressoIdlingResource {

    private val CLASS_NAME = "EspressoIdlingResource"

    private const val RESOURCE = "GLOBAL"

    @JvmField val countingIdlingResource = CountingIdlingResource(RESOURCE)

    fun increment() {
        printLogD(CLASS_NAME, "INCREMENTING.")
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            printLogD(CLASS_NAME, "DECREMENTING.")
            countingIdlingResource.decrement()
        }
    }

    fun clear() {
        if (!countingIdlingResource.isIdleNow) {
            decrement()
            clear()
        }
    }
}








