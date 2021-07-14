package com.yolo.fun_habit_journal.framework.presentation.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import coil.ImageLoader
import coil.request.Disposable
import coil.request.ImageRequest
import com.google.firebase.auth.FirebaseAuth
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habit_journal.databinding.FragmentSplashBinding
import com.yolo.fun_habit_journal.framework.datasource.network.EMAIL
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.android.synthetic.main.fragment_splash.splash_fragment_container
import kotlinx.android.synthetic.main.fragment_splash.splash_icon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val SPLASH_IMAGE_URL = "https://image.flaticon.com/icons/png/512/4310/4310163.png"

@FlowPreview
@ExperimentalCoroutinesApi
class SplashFragment
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseFragment() {

    private lateinit var binding: FragmentSplashBinding

    private var disposable: Disposable? = null

    private val viewModel: SplashViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_splash,
            container,
            false
        )

        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkFirebaseAuth()
        setupSplashAnimation()
    }

    private fun setupSplashAnimation() {
        val imageLoader = ImageLoader(requireContext())
        val request = ImageRequest.Builder(requireContext())
            .data(SPLASH_IMAGE_URL)
            .target { drawable ->
                splash_icon.setImageDrawable(drawable)
                splash_fragment_container.transitionToEnd()
            }.build()

        disposable = imageLoader.enqueue(request)
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    private fun checkFirebaseAuth() {
        if (FirebaseAuth.getInstance().currentUser == null) {
            displayCapturePassword()
        } else {
            subscribeObservers()
        }
    }

    // add password hardcoded input
    private fun displayCapturePassword() {
        uiController.displayInputCaptureDialog(
            getString(R.string.text_enter_password),
            object : DialogInputCaptureCallback {
                override fun onTextCaptured(text: String) {
                    FirebaseAuth.getInstance()
                        .signInWithEmailAndPassword(EMAIL, text)
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                printLogD(
                                    "MainActivity",
                                    "Signing in to Firebase: ${it.result}"
                                )
                                subscribeObservers()
                            }
                        }
                }
            }
        )
    }

    private fun subscribeObservers() {
        viewModel.hasSyncBeenExecuted().observe(viewLifecycleOwner, Observer { hasSyncBeenExecuted ->
            if (hasSyncBeenExecuted) {
                navHabitListFragment()
            }
        })
    }

    private fun navHabitListFragment() {
        findNavController().navigate(R.id.action_splashFragment_to_habitListFragment)
    }

}

