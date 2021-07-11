package com.yolo.fun_habit_journal.framework.presentation.habitdetail

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.AreYouSureCallback
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.usecases.common.usecase.DELETE_ARE_YOU_SURE
import com.yolo.fun_habit_journal.business.usecases.common.usecase.DELETE_HABIT_SUCCESS
import com.yolo.fun_habit_journal.business.usecases.habitdetail.usecase.UPDATE_HABIT_SUCCESS
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD
import com.yolo.fun_habit_journal.framework.presentation.common.disableContentInteraction
import com.yolo.fun_habit_journal.framework.presentation.common.enableContentInteraction
import com.yolo.fun_habit_journal.framework.presentation.common.fadeIn
import com.yolo.fun_habit_journal.framework.presentation.common.fadeOut
import com.yolo.fun_habit_journal.framework.presentation.common.hideKeyboard
import com.yolo.fun_habit_journal.framework.presentation.common.showKeyboard
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.CollapsingToolbarState
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitDetailViewState
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.state.HabitInteractionState
import com.yolo.fun_habit_journal.framework.presentation.habitlist.HABIT_PENDING_DELETE_BUNDLE_KEY
import com.yydcdut.markdown.MarkdownProcessor
import com.yydcdut.markdown.syntax.edit.EditFactory
import kotlinx.android.synthetic.main.fragment_habit_detail.app_bar
import kotlinx.android.synthetic.main.fragment_habit_detail.container_due_date
import kotlinx.android.synthetic.main.fragment_habit_detail.habit_body
import kotlinx.android.synthetic.main.fragment_habit_detail.habit_title
import kotlinx.android.synthetic.main.layout_habit_detail_toolbar.tool_bar_title
import kotlinx.android.synthetic.main.layout_habit_detail_toolbar.toolbar_primary_icon
import kotlinx.android.synthetic.main.layout_habit_detail_toolbar.toolbar_secondary_icon
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val HABIT_DETAIL_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitdetail.state"
const val HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY = "selectedHabit"
const val HABIT_TITLE_CANNOT_BE_EMPTY = "Habit title can not be empty."

@ExperimentalCoroutinesApi
@FlowPreview
class HabitDetailFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory
) : BaseFragment(R.layout.fragment_habit_detail) {

    val viewModel: HabitDetailViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupOnBackPressDispatcher()
        subscribeObservers()

        container_due_date.setOnClickListener {
            // TODO("handle click of due date")
        }

        habit_title.setOnClickListener {
            onClickHabitTitle()
        }

        habit_body.setOnClickListener {
            onClickHabitBody()
        }

        setupMarkdown()
        getSelectedNoteFromPreviousFragment()
        restoreInstanceState()
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

    private fun onClickHabitTitle() {
        if (!viewModel.isEditingTitle()) {
            updateBodyInViewModel()
            updateHabit()
            viewModel.setNoteInteractionTitleState(HabitInteractionState.EditState())
        }
    }

    private fun onClickHabitBody() {
        if (!viewModel.isEditingBody()) {
            updateTitleInViewModel()
            updateHabit()
            viewModel.setNoteInteractionBodyState(HabitInteractionState.EditState())
        }
    }

    private fun onBackPressed() {
        view?.hideKeyboard()
        if (viewModel.checkEditState()) {
            updateBodyInViewModel()
            updateTitleInViewModel()
            updateHabit()
            viewModel.exitEditState()
            displayDefaultToolbar()
        } else {
            findNavController().popBackStack()
        }
    }

    override fun onPause() {
        super.onPause()
        updateTitleInViewModel()
        updateBodyInViewModel()
        updateHabit()
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
                        viewModel.setIsUpdatePending(false)
                        viewModel.clearStateMessage()
                    }

                    DELETE_HABIT_SUCCESS -> {
                        viewModel.clearStateMessage()
                        onDeleteSuccess()
                    }

                    else -> {
                        uiController.onResponseReceived(
                            response = stateMessage.response,
                            stateMessageCallback = object : StateMessageCallback {
                                override fun removeMessageFromStack() {
                                    viewModel.clearStateMessage()
                                }
                            }
                        )
                        when (response.message) {
                            HABIT_DETAIL_ERROR_RETRIEVEING_SELECTED_HABIT -> {
                                findNavController().popBackStack()
                            }
                            else -> {
                                // do nothing
                            }
                        }
                    }
                }
            }

        })

        viewModel.collapsingToolbarState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is CollapsingToolbarState.Expanded -> {
                    transitionToExpandedMode()
                }

                is CollapsingToolbarState.Collapsed -> {
                    transitionToCollapsedMode()
                }
            }
        })

        viewModel.habitTitleInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is HabitInteractionState.EditState -> {
                    habit_title.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is HabitInteractionState.DefaultState -> {
                    habit_title.disableContentInteraction()
                }
            }
        })

        viewModel.habitBodyInteractionState.observe(viewLifecycleOwner, Observer { state ->

            when (state) {

                is HabitInteractionState.EditState -> {
                    habit_body.enableContentInteraction()
                    view?.showKeyboard()
                    displayEditStateToolbar()
                    viewModel.setIsUpdatePending(true)
                }

                is HabitInteractionState.DefaultState -> {
                    habit_body.disableContentInteraction()
                }
            }
        })
    }

    private fun displayDefaultToolbar() {
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_arrow_back_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_delete,
                    a.application.theme
                )
            )
        }
    }

    private fun displayEditStateToolbar() {
        activity?.let { a ->
            toolbar_primary_icon.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_close_grey_24dp,
                    a.application.theme
                )
            )
            toolbar_secondary_icon.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_done_grey_24dp,
                    a.application.theme
                )
            )
        }
    }

    private fun setHabitTitle(title: String) {
        habit_title.setText(title)
    }

    private fun getHabitTitle(): String {
        return habit_title.text.toString()
    }

    private fun getHabitBody(): String {
        return habit_body.text.toString()
    }

    private fun setHabitBody(body: String?) {
        habit_body.setText(body)
    }

    private fun getSelectedNoteFromPreviousFragment() {
        arguments?.let { args ->
            (args.getParcelable(HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY) as Habit?)?.let { selectedHabit ->
                viewModel.setHabit(selectedHabit)
            } ?: onErrorRetrievingHabitFromPreviousFragment()
        }

    }

    private fun restoreInstanceState() {
        arguments?.let { args ->
            (args.getParcelable(HABIT_DETAIL_STATE_BUNDLE_KEY) as HabitDetailViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)

                // One-time check after rotation
                if (viewModel.isToolbarCollapsed()) {
                    app_bar.setExpanded(false)
                    transitionToCollapsedMode()
                } else {
                    app_bar.setExpanded(true)
                    transitionToExpandedMode()
                }
            }
        }
    }

    private fun updateTitleInViewModel() {
        if (viewModel.isEditingTitle()) {
            viewModel.updateHabitTitle(getHabitTitle())
        }
    }

    private fun updateBodyInViewModel() {
        if (viewModel.isEditingBody()) {
            viewModel.updateHabitBody(getHabitBody())
        }
    }

    private fun setupUI() {
        habit_title.disableContentInteraction()
        habit_body.disableContentInteraction()
        displayDefaultToolbar()
        transitionToExpandedMode()

        app_bar.addOnOffsetChangedListener(
            AppBarLayout.OnOffsetChangedListener { _, offset ->

                if (offset < COLLAPSING_TOOLBAR_VISIBILITY_THRESHOLD) {
                    updateTitleInViewModel()
                    if (viewModel.isEditingTitle()) {
                        viewModel.exitEditState()
                        displayDefaultToolbar()
                        updateHabit()
                    }
                    viewModel.setCollapsingToolbarState(CollapsingToolbarState.Collapsed())
                } else {
                    viewModel.setCollapsingToolbarState(CollapsingToolbarState.Expanded())
                }
            })

        toolbar_primary_icon.setOnClickListener {
            if (viewModel.checkEditState()) {
                view?.hideKeyboard()
                viewModel.triggerHabitObservers()
                viewModel.exitEditState()
                displayDefaultToolbar()
            } else {
                onBackPressed()
            }
        }

        toolbar_secondary_icon.setOnClickListener {
            if (viewModel.checkEditState()) {
                view?.hideKeyboard()
                updateTitleInViewModel()
                updateBodyInViewModel()
                updateHabit()
                viewModel.exitEditState()
                displayDefaultToolbar()
            } else {
                deleteNote()
            }
        }
    }

    private fun deleteNote() {
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
        val bundle = bundleOf(HABIT_PENDING_DELETE_BUNDLE_KEY to viewModel.getHabit())
        viewModel.setHabit(null) // clear note from ViewState
        viewModel.setIsUpdatePending(false) // prevent update onPause
        findNavController().navigate(
            R.id.action_habit_detail_fragment_to_habitListFragment,
            bundle
        )
    }

    private fun setupOnBackPressDispatcher() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                onBackPressed()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
    }


    private fun updateHabit() {
        if (viewModel.getIsUpdatePending()) {
            viewModel.setStateEvent(
                HabitDetailStateEvent.UpdateHabitEvent()
            )
        }
    }

    private fun transitionToCollapsedMode() {
        habit_title.fadeOut()
        displayToolbarTitle(tool_bar_title, getHabitTitle(), true)
    }

    private fun transitionToExpandedMode() {
        habit_title.fadeIn()
        displayToolbarTitle(tool_bar_title, null, true)
    }

    override fun inject() {}

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.getCurrentViewStateOrNew()
        outState.putParcelable(HABIT_DETAIL_STATE_BUNDLE_KEY, viewState)
        super.onSaveInstanceState(outState)
    }
}
