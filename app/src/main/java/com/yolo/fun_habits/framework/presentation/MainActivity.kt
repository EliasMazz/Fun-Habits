package com.yolo.fun_habits.framework.presentation

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.callbacks.onDismiss
import com.afollestad.materialdialogs.input.input
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.yolo.fun_habits.R
import com.yolo.fun_habits.business.domain.state.AreYouSureCallback
import com.yolo.fun_habits.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habits.business.domain.state.MessageType
import com.yolo.fun_habits.business.domain.state.Response
import com.yolo.fun_habits.business.domain.state.SnackbarUndoCallback
import com.yolo.fun_habits.business.domain.state.SnackbarUndoListener
import com.yolo.fun_habits.business.domain.state.StateMessageCallback
import com.yolo.fun_habits.business.domain.state.UIComponentType
import com.yolo.fun_habits.framework.presentation.common.HabitFragmentFactory
import com.yolo.fun_habits.framework.presentation.common.displayToast
import com.yolo.fun_habits.framework.presentation.common.gone
import com.yolo.fun_habits.framework.presentation.common.visible
import com.yolo.fun_habits.framework.util.TodoCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

@ExperimentalCoroutinesApi
@FlowPreview
class MainActivity : AppCompatActivity(), UIController {

    private val TAG: String = "MainActivity"

    private var appBarConfiguration: AppBarConfiguration? = null

    private var dialogInView: MaterialDialog? = null

    @Inject
    lateinit var fragmentFactory: HabitFragmentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        inject()
        setFragmentFactory()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

    }

    private fun setFragmentFactory() {
        supportFragmentManager.fragmentFactory = fragmentFactory
    }

    private fun inject() {
        (application as BaseApplication).appComponent.inject(this)
    }

    override fun displayProgressBar(isDisplayed: Boolean) =
        if (isDisplayed) {
            //main_progress_bar.visible()
        } else {
            //main_progress_bar.gone()
        }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment)
            .navigateUp(appBarConfiguration as AppBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun displayInputCaptureDialog(
        title: String,
        callback: DialogInputCaptureCallback
    ) {
        dialogInView = MaterialDialog(this).show {
            title(text = title)
            input(
                waitForPositiveButton = true,
                inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            ) { _, text ->
                callback.onTextCaptured(text.toString())
            }
            positiveButton(R.string.text_ok)
            onDismiss {
                dialogInView = null
            }
            cancelable(true)
        }
    }

    override fun onResponseReceived(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {

        when (response.uiComponentType) {

            is UIComponentType.SnackBar -> {
                val onDismissCallback: TodoCallback? = response.uiComponentType.onDismissCallback
                val undoCallback: SnackbarUndoCallback? = response.uiComponentType.undoCallback

                response.message?.let { msg ->
                    displaySnackbar(
                        message = msg,
                        snackbarUndoCallback = undoCallback,
                        onDismissCallback = onDismissCallback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.AreYouSureDialog -> {
                response.message?.let {
                    areYouSureDialog(
                        message = it,
                        callback = response.uiComponentType.callback,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Toast -> {
                response.message?.let {
                    displayToast(
                        message = it,
                        stateMessageCallback = stateMessageCallback
                    )
                }
            }

            is UIComponentType.Dialog -> {
                displayDialog(
                    response = response,
                    stateMessageCallback = stateMessageCallback
                )
            }

            is UIComponentType.None -> {
                // TODO send error report
                stateMessageCallback.removeMessageFromStack()
            }
        }
    }

    private fun displaySnackbar(
        message: String,
        snackbarUndoCallback: SnackbarUndoCallback?,
        onDismissCallback: TodoCallback?,
        stateMessageCallback: StateMessageCallback
    ) {
        val snackbar = Snackbar.make(
            findViewById(R.id.main_container),
            message,
            Snackbar.LENGTH_LONG
        )
        snackbar.setAction(
            getString(R.string.text_undo),
            SnackbarUndoListener(snackbarUndoCallback)
        )
        snackbar.addCallback(object : BaseTransientBottomBar.BaseCallback<Snackbar>() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                onDismissCallback?.execute()
            }
        })
        snackbar.show()
        stateMessageCallback.removeMessageFromStack()
    }

    private fun displayDialog(
        response: Response,
        stateMessageCallback: StateMessageCallback
    ) {
        response.message?.let { message ->

            dialogInView = when (response.messageType) {

                is MessageType.Error -> {
                    displayErrorDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Success -> {
                    displaySuccessDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                is MessageType.Info -> {
                    displayInfoDialog(
                        message = message,
                        stateMessageCallback = stateMessageCallback
                    )
                }

                else -> {
                    // do nothing
                    stateMessageCallback.removeMessageFromStack()
                    null
                }
            }
        } ?: stateMessageCallback.removeMessageFromStack()
    }

    override fun hideSoftKeyboard() {
        if (currentFocus != null) {
            val inputMethodManager = getSystemService(
                Context.INPUT_METHOD_SERVICE
            ) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        }
    }

    override fun onPause() {
        super.onPause()
        if (dialogInView != null) {
            (dialogInView as MaterialDialog).dismiss()
            dialogInView = null
        }
    }

    private fun displaySuccessDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_success)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun displayErrorDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_error)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun displayInfoDialog(
        message: String?,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.text_info)
                message(text = message)
                positiveButton(R.string.text_ok) {
                    stateMessageCallback.removeMessageFromStack()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

    private fun areYouSureDialog(
        message: String,
        callback: AreYouSureCallback,
        stateMessageCallback: StateMessageCallback
    ): MaterialDialog {
        return MaterialDialog(this)
            .show {
                title(R.string.are_you_sure)
                message(text = message)
                negativeButton(R.string.text_cancel) {
                    stateMessageCallback.removeMessageFromStack()
                    callback.cancel()
                    dismiss()
                }
                positiveButton(R.string.text_yes) {
                    stateMessageCallback.removeMessageFromStack()
                    callback.proceed()
                    dismiss()
                }
                onDismiss {
                    dialogInView = null
                }
                cancelable(false)
            }
    }

}
