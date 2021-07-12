package com.yolo.fun_habit_journal.framework.presentation.common

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.framework.util.TodoCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun EditText.disableContentInteraction() {
    keyListener = null
    isFocusable = false
    isFocusableInTouchMode = false
    isCursorVisible = false
    setBackgroundResource(android.R.color.transparent)
    clearFocus()
}

fun EditText.enableContentInteraction() {
    keyListener = EditText(context).keyListener
    isFocusable = true
    isFocusableInTouchMode = true
    isCursorVisible = true
    setBackgroundResource(android.R.color.white)
    requestFocus()
    if(text != null){
        setSelection(text.length)
    }
}

fun Activity.displayToast(
    @StringRes message:Int,
    stateMessageCallback: StateMessageCallback
){
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    stateMessageCallback.removeMessageFromStack()
}

fun Activity.displayToast(
    message:String,
    stateMessageCallback: StateMessageCallback
){
    Toast.makeText(this,message, Toast.LENGTH_SHORT).show()
    stateMessageCallback.removeMessageFromStack()
}
