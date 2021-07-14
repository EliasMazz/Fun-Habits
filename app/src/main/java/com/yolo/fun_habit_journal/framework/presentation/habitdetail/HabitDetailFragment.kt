package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.AreYouSureCallback
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.DELETE_ARE_YOU_SURE
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.DELETE_HABIT_SUCCESS
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.UPDATE_HABIT_SUCCESS
import com.yolo.fun_habit_journal.databinding.FragmentHabitDetailBinding
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.hideKeyboard
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.edit.EditFactory
import kotlinx.android.synthetic.main.fragment_habit_detail.habit_body
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val HABIT_DETAIL_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitdetail.state"
const val HABIT_DETAIL_STATE_DESCRIPTION_BUNDLE_KEY = "state_habit_description_restore"
const val HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY = "selectedHabit"
const val HABIT_TITLE_CANNOT_BE_EMPTY = "Habit title can not be empty."

@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseFragment() {

    val viewModel: HabitDetailViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var binding: FragmentHabitDetailBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_habit_detail, container,
            false
        )
        binding.viewModel = viewModel

        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupDataStateManager()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupOnBackPressDispatcher()
        subscribeObservers()
        setupMarkdown()

        if (savedInstanceState == null) {
            getSelectedNoteFromPreviousFragment()
        } else {
            restoreInstanceState()
        }
    }

    private fun onErrorRetrievingHabitFromPreviousFragment() {
        viewModel.setStateEvent(
            HabitDetailStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = HABIT_DETAIL_ERROR_RETRIEVEING_SELECTED_HABIT,
                        uiComponentType = UIComponentType.Dialog,
                        messageType = MessageType.Error
                    )
                )
            )
        )
    }

    private fun setupMarkdown() {
        activity?.run {
            val markdownProcessor = MarkdownProcessor(this)
            markdownProcessor.factory(EditFactory.create())
            markdownProcessor.live(habit_body)
        }
    }

    private fun onBackPressed() {
        view?.hideKeyboard()
        findNavController().popBackStack()
    }

    private fun subscribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                viewState.habit?.let { habit ->
                    setHabitTitle(habit.title)
                    setHabitBody(habit.body)
                }
            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->

            stateMessage?.response?.let { response ->
                when (response.message) {
                    UPDATE_HABIT_SUCCESS -> {
                        findNavController().popBackStack()
                    }

                    DELETE_HABIT_SUCCESS -> {
                        onDeleteSuccess()
                    }

                    HABIT_DETAIL_ERROR_RETRIEVEING_SELECTED_HABIT -> {
                        findNavController().popBackStack()
                    }
                    else -> {
                        //Do nothing
                    }
                }
                delegateStateMessageToActivity(stateMessage)
            }
        })
    }

    private fun delegateStateMessageToActivity(stateMessage: StateMessage) {
        uiController.onResponseReceived(
            response = stateMessage.response,
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    private fun setHabitTitle(title: String) {
        binding.habitTitle.setText(title)
    }

    private fun setHabitBody(body: String) {
        binding.habitBody.setText(body)
    }

    private fun getSelectedNoteFromPreviousFragment() {
        arguments?.let { args ->
            (args.getParcelable(HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY) as Habit?)?.let { selectedHabit ->
                viewModel.setHabit(selectedHabit)
            } ?: onErrorRetrievingHabitFromPreviousFragment()
        }
    }


    private fun setupUI() {
        binding.toolbarBackButton.setOnClickListener {
            onBackPressed()
        }

        binding.toolbarDeleteButton.setOnClickListener {
            deleteHabit()
        }

        binding.toolbarSaveButton.setOnClickListener {
            updateHabit()
        }
    }

    private fun deleteHabit() {
        viewModel.setStateEvent(
            HabitDetailStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.getHabit()?.let { habit ->
                                        initiateDeleteTransaction(habit)
                                    }
                                }

                                override fun cancel() {
                                    // do nothing
                                }
                            }
                        ),
                        messageType = MessageType.Info
                    )
                )
            )
        )
    }

    private fun initiateDeleteTransaction(habit: Habit) {
        viewModel.beginPendingDelete(habit)
    }

    private fun onDeleteSuccess() {
        viewModel.setHabit(null) // clear note from ViewState
        findNavController().navigate(R.id.action_habit_detail_fragment_to_habitListFragment)
    }

    private fun setupOnBackPressDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val restoreHabit = viewModel.getHabit()?.copy(
            title = getHabitTitle(),
            body = getHabitBody()
        )
        viewModel.setHabit(restoreHabit)
        outState.putParcelable(HABIT_DETAIL_STATE_BUNDLE_KEY, restoreHabit)
        super.onSaveInstanceState(outState)
    }

    private fun restoreInstanceState() {
        (arguments?.getParcelable(HABIT_DETAIL_STATE_BUNDLE_KEY) as Habit?)?.let { habit ->
            viewModel.setHabit(habit)
        }
    }

    private fun updateHabit() {
        viewModel.updateHabit(getHabitTitle(), getHabitBody())
        viewModel.setStateEvent(HabitDetailStateEvent.UpdateHabitEvent())
    }

    private fun getHabitTitle(): String =
        binding.habitTitle.text.toString()

    private fun getHabitBody(): String =
        binding.habitBody.text.toString()
}
