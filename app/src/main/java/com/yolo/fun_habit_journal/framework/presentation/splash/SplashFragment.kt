package com.yolo.fun_habit_journal.framework.presentation.splash

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habit_journal.framework.datasource.network.EMAIL
import com.yolo.fun_habit_journal.framework.presentation.BaseApplication
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): BaseFragment(R.layout.fragment_splash) {

    private val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
    }

    private fun checkFirebaseAuth(){
        if(FirebaseAuth.getInstance().currentUser == null){
            displayCapturePassword()
        }
        else{
            subscribeObservers()
        }
    }

    // add password input
    private fun displayCapturePassword(){
        uiController.displayInputCaptureDialog(
            getString(R.string.text_enter_password),
            object: DialogInputCaptureCallback {
                override fun onTextCaptured(text: String) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(EMAIL, text)
                        .addOnCompleteListener {
                            if(it.isSuccessful){
                                printLogD("MainActivity",
                                    "Signing in to Firebase: ${it.result}")
                                subscribeObservers()
                            }
                        }
                }
            }
        )
    }

    private fun subscribeObservers(){
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, Observer { hasSyncBeenExecuted ->
            if(hasSyncBeenExecuted){
                navNoteListFragment()
            }
        })
    }

    private fun navNoteListFragment(){
        findNavController().navigate(R.id.action_splashFragment_to_habitListFragment)
    }

    override fun inject() {
        getAppComponent().inject(this)
    }
}

