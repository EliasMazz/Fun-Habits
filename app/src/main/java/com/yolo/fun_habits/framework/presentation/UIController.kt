package com.yolo.fun_habits.framework.presentation

import com.yolo.fun_habits.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.StateMessageCallback

interface UIController {

    fun displayProgressBar(isDisplayed: Boolean)

    fun hideSoftKeyboard()

    fun displayInputCaptureDialog(title: String, callback: DialogInputCaptureCallback)

    fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    )
}

