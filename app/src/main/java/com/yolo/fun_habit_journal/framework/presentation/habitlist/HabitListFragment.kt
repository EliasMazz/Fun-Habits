package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.afollestad.materialdialogs.customview.getCustomView
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.AreYouSureCallback
import com.yolo.fun_habit_journal.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habit_journal.business.domain.state.MessageType
import com.yolo.fun_habit_journal.business.domain.state.Response
import com.yolo.fun_habit_journal.business.domain.state.SnackbarUndoCallback
import com.yolo.fun_habit_journal.business.domain.state.StateMessage
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.business.domain.state.UIComponentType
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.business.usecases.common.usecase.DELETE_HABIT_PENDING
import com.yolo.fun_habit_journal.business.usecases.common.usecase.DELETE_HABIT_SUCCESS
import com.yolo.fun_habit_journal.business.usecases.habitlist.usecase.DELETE_HABITS_ARE_YOU_SURE
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_FILTER_DATE_CREATED
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_FILTER_TITLE
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_ORDER_ASC
import com.yolo.fun_habit_journal.framework.datasource.database.HABIT_ORDER_DESC
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.hideKeyboard
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListToolbarState.*
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import com.yolo.fun_habit_journal.framework.util.TodoCallback
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.android.synthetic.main.fragment_habit_list.add_new_habit_fab
import kotlinx.android.synthetic.main.fragment_habit_list.recycler_view
import kotlinx.android.synthetic.main.fragment_habit_list.swipe_refresh
import kotlinx.android.synthetic.main.fragment_habit_list.toolbar_content_container
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val HABIT_LIST_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitlist.state"

@ExperimentalCoroutinesApi
@FlowPreview
class HabitListFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : BaseFragment(R.layout.fragment_habit_list), HabitListAdapter.Interaction, ItemTouchHelperAdapter {

    val viewModel: HabitListViewModel by viewModels {
        viewModelFactory
    }

    private var listAdapter: HabitListAdapter? = null
    private var itemTouchHelper: ItemTouchHelper? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupChannel()
        arguments?.let { args ->
            args.getParcelable<Habit>(HABIT_PENDING_DELETE_BUNDLE_KEY)?.let { note ->
                viewModel.setHabitPendingDelete(note)
                showUndoSnackbarAndUndoDelete()
                clearArgs()
            }
        }
    }

    private fun clearArgs() {
        arguments?.clear()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupSwipeRefresh()
        setupFAB()
        subscribeObservers()

        restoreInstanceState(savedInstanceState)
    }

    private fun subscribeObservers() {

        viewModel.toolbarState.observe(viewLifecycleOwner, Observer { toolbarState ->

            when (toolbarState) {

                is MultiSelectionState -> {
                    enableMultiSelectToolbarState()
                    disableSearchViewToolbarState()
                }

                is SearchViewState -> {
                    enableSearchViewToolbarState()
                    disableMultiSelectToolbarState()
                }
            }
        })

        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->

            if (viewState != null) {
                viewState.habitList?.let { habitList ->
                    if (viewModel.isPaginationExhausted()
                        && !viewModel.isQueryExhausted()
                    ) {
                        viewModel.setQueryExhausted(true)
                    }
                    listAdapter?.submitList(habitList)
                    listAdapter?.notifyDataSetChanged()
                }


                viewState.newHabit?.let { newHabit ->
                    navigateToDetailFragment(newHabit)
                }
            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
            printActiveJobs()
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let { message ->
                if (message.response.message?.equals(DELETE_HABIT_SUCCESS) == true) {
                    showUndoSnackbarAndUndoDelete()
                } else {
                    uiController.onResponseReceived(
                        response = message.response,
                        stateMessageCallback = object : StateMessageCallback {
                            override fun removeMessageFromStack() {
                                viewModel.clearStateMessage()
                            }
                        }
                    )
                }
            }
        })
    }

    private fun restoreInstanceState(savedInstanceState: Bundle?) {
        savedInstanceState?.let { inState ->
            (inState[HABIT_LIST_STATE_BUNDLE_KEY] as HabitListViewState?)?.let { viewState ->
                viewModel.setViewState(viewState)
            }
        }
    }

    private fun setupRecyclerView() {
        recycler_view.apply {
            layoutManager = LinearLayoutManager(activity)
            val topSpacingDecorator = TopSpacingItemDecoration(20)
            addItemDecoration(topSpacingDecorator)

            itemTouchHelper = ItemTouchHelper(
                HabitItemTouchHelperCallback(
                    this@HabitListFragment,
                    viewModel.habitListInteractionManager
                )
            )

            listAdapter = HabitListAdapter(
                this@HabitListFragment,
                viewLifecycleOwner,
                viewModel.habitListInteractionManager.selectedHabits,
                dateUtil
            )

            itemTouchHelper?.attachToRecyclerView(this)

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val lastPosition = layoutManager.findLastVisibleItemPosition()
                    if (lastPosition == listAdapter?.itemCount?.minus(1)) {
                        viewModel.nextPage()
                    }
                }
            })
            adapter = listAdapter
        }
    }

    private fun setupSearchView() {
        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById<Toolbar>(R.id.searchview_toolbar)

        searchViewToolbar?.let { toolbar ->

            val searchView = toolbar.findViewById<SearchView>(R.id.search_view)

            val searchPlate: SearchView.SearchAutoComplete? = searchView.findViewById(androidx.appcompat.R.id.search_src_text)

            searchPlate?.setOnEditorActionListener { v, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                    || actionId == EditorInfo.IME_ACTION_SEARCH
                ) {
                    val searchQuery = v.text.toString()
                    viewModel.setQuery(searchQuery)
                    startNewSearch()
                }
                true
            }
        }
    }

    private fun startNewSearch() {
        viewModel.clearList()
        viewModel.loadFirstPage()
    }

    private fun setupSwipeRefresh() {
        swipe_refresh.setOnRefreshListener {
            startNewSearch()
            swipe_refresh.isRefreshing = false
        }
    }

    private fun setupFAB() {
        add_new_habit_fab.setOnClickListener {
            uiController.displayInputCaptureDialog(
                getString(R.string.text_enter_a_title),
                object : DialogInputCaptureCallback {
                    override fun onTextCaptured(text: String) {
                        val newNote = viewModel.createNewHabit(title = text)
                        viewModel.setStateEvent(
                            HabitListStateEvent.InsertNewHabitEvent(
                                title = newNote.title
                            )
                        )
                    }
                }
            )
        }
    }

    fun showFilterDialog() {
        activity?.let {
            val dialog = MaterialDialog(it)
                .noAutoDismiss()
                .customView(R.layout.layout_filter)

            val view = dialog.getCustomView()

            val filter = viewModel.getFilter()
            val order = viewModel.getOrder()

            view.findViewById<RadioGroup>(R.id.filter_group).apply {
                when (filter) {
                    HABIT_FILTER_DATE_CREATED -> check(R.id.filter_date)
                    HABIT_FILTER_TITLE -> check(R.id.filter_title)
                }
            }

            view.findViewById<RadioGroup>(R.id.order_group).apply {
                when (order) {
                    HABIT_ORDER_ASC -> check(R.id.filter_asc)
                    HABIT_ORDER_DESC -> check(R.id.filter_desc)
                }
            }

            view.findViewById<TextView>(R.id.positive_button).setOnClickListener {

                val newFilter =
                    when (view.findViewById<RadioGroup>(R.id.filter_group).checkedRadioButtonId) {
                        R.id.filter_title -> HABIT_FILTER_TITLE
                        R.id.filter_date -> HABIT_FILTER_DATE_CREATED
                        else -> HABIT_FILTER_DATE_CREATED
                    }

                val newOrder =
                    when (view.findViewById<RadioGroup>(R.id.order_group).checkedRadioButtonId) {
                        R.id.filter_desc -> "-"
                        else -> ""
                    }

                viewModel.apply {
                    saveFilterOptions(newFilter, newOrder)
                    setHabitFilter(newFilter)
                    setHabitOrder(newOrder)
                }

                startNewSearch()

                dialog.dismiss()
            }

            view.findViewById<TextView>(R.id.negative_button).setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun setupFilterButton() {
        val searchViewToolbar: Toolbar? = toolbar_content_container
            .findViewById<Toolbar>(R.id.searchview_toolbar)
        searchViewToolbar?.findViewById<ImageView>(R.id.action_filter)?.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showUndoSnackbarAndUndoDelete() {
        uiController.onResponseReceived(
            response = Response(
                message = DELETE_HABIT_PENDING,
                uiComponentType = UIComponentType.SnackBar(
                    undoCallback = object : SnackbarUndoCallback {
                        override fun undo() {
                            viewModel.undoDelete()
                        }
                    },
                    onDismissCallback = object : TodoCallback {
                        override fun execute() {
                            viewModel.setHabitPendingDelete(null)
                        }
                    }
                ),
                messageType = MessageType.Info
            ),
            stateMessageCallback = object : StateMessageCallback {
                override fun removeMessageFromStack() {
                    viewModel.clearStateMessage()
                }
            }
        )
    }

    // for debugging
    private fun printActiveJobs() {

        for ((index, job) in viewModel.getActiveJobs().withIndex()) {
            printLogD(
                "HabitList",
                "${index}: ${job}"
            )
        }
    }

    private fun navigateToDetailFragment(selectedHabit: Habit) {
        val bundle = bundleOf(HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY to selectedHabit)
        findNavController().navigate(
            R.id.action_habit_list_fragment_to_habitDetailFragment,
            bundle
        )
        viewModel.setHabit(null)
    }

    private fun setupUI() {
        view?.hideKeyboard()
    }

    private fun saveLayoutManagerState() {
        recycler_view.layoutManager?.onSaveInstanceState()?.let { lmState ->
            viewModel.setLayoutManagerState(lmState)
        }
    }

    private fun enableMultiSelectToolbarState() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_multiselection_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupMultiSelectionToolbar(view)
        }
    }

    private fun setupMultiSelectionToolbar(parentView: View) {
        parentView
            .findViewById<ImageView>(R.id.action_exit_multiselect_state)
            .setOnClickListener {
                viewModel.setToolbarState(SearchViewState())
            }

        parentView
            .findViewById<ImageView>(R.id.action_delete_habit)
            .setOnClickListener {
                deleteNotes()
            }
    }


    private fun enableSearchViewToolbarState() {
        view?.let { v ->
            val view = View.inflate(
                v.context,
                R.layout.layout_searchview_toolbar,
                null
            )
            view.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            toolbar_content_container.addView(view)
            setupSearchView()
            setupFilterButton()
        }
    }

    private fun disableMultiSelectToolbarState() {
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.multiselect_toolbar)
            toolbar_content_container.removeView(view)
            viewModel.clearSelectedHabits()
        }
    }

    private fun disableSearchViewToolbarState() {
        view?.let {
            val view = toolbar_content_container
                .findViewById<Toolbar>(R.id.searchview_toolbar)
            toolbar_content_container.removeView(view)
        }
    }

    override fun inject() {}

    override fun onResume() {
        super.onResume()
        viewModel.retrieveHabitListCountInCache()
        viewModel.clearList()
        viewModel.refreshSearchQuery()

    }

    override fun onPause() {
        super.onPause()
        saveLayoutManagerState()
    }

    // I didn't use the "SavedStateHandle" here is not working for testing
    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        //clear the list Don't want to save a large list to bundle.
        viewState?.habitList = ArrayList()

        outState.putParcelable(
            HABIT_LIST_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun restoreListPosition() {
        viewModel.getLayoutManagerState()?.let { lmState ->
            recycler_view?.layoutManager?.onRestoreInstanceState(lmState)
        }
    }

    override fun isMultiSelectionModeEnabled() = viewModel.isMultiSelectionStateActive()

    override fun activateMultiSelectionMode() = viewModel.setToolbarState(MultiSelectionState())

    override fun onItemSelected(position: Int, item: Habit) {
        if (isMultiSelectionModeEnabled()) {
            viewModel.addOrRemoveHabitFromSelectedList(item)
        } else {
            viewModel.setHabit(item)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null // can leak memory
        itemTouchHelper = null // can leak memory
    }

    override fun isHabitSelected(habit: Habit): Boolean {
        return viewModel.isHabitSelected(habit)
    }

    override fun onItemSwiped(position: Int) {
        if (!viewModel.isDeletePending()) {
            listAdapter?.getHabit(position)?.let { note ->
                viewModel.beginPendingDelete(note)
            }
        } else {
            listAdapter?.notifyDataSetChanged()
        }
    }

    private fun deleteNotes() {
        viewModel.setStateEvent(
            HabitListStateEvent.CreateStateMessageEvent(
                stateMessage = StateMessage(
                    response = Response(
                        message = DELETE_HABITS_ARE_YOU_SURE,
                        uiComponentType = UIComponentType.AreYouSureDialog(
                            object : AreYouSureCallback {
                                override fun proceed() {
                                    viewModel.deleteNotes()
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
}
