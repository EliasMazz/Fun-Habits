package com.yolo.fun_habit_journal.framework.presentation.common

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.VisibleForTesting
import androidx.fragment.app.Fragment
import com.yolo.fun_habit_journal.dependencyinjection.AppComponent
import com.yolo.fun_habit_journal.framework.presentation.BaseApplication
import com.yolo.fun_habit_journal.framework.presentation.MainActivity
import com.yolo.fun_habit_journal.framework.presentation.UIController
import com.yolo.fun_habit_journal.framework.util.TodoCallback
import java.lang.ClassCastException

abstract class BaseFragment : Fragment() {
    lateinit var uiController: UIController

    private fun showToolbarTitle(
        textView: TextView,
        title: String
    ){
        textView.text = title
        textView.visible()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setUIController(null) // null in production
    }

    @VisibleForTesting
    fun setUIController(mockController: UIController?){

        // TEST: Set interface from mock
        if(mockController != null){
            this.uiController = mockController
        }
        else{ // PRODUCTION: if no mock, get from context
            activity?.let {
                if(it is MainActivity){
                    try{
                        uiController = context as UIController
                    }catch (e: ClassCastException){
                        e.printStackTrace()
                    }
                }
            }
        }
    }
}
