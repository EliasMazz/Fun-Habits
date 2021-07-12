package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.state.DialogInputCaptureCallback
import com.yolo.fun_habit_journal.business.domain.state.StateMessageCallback
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.presentation.common.BaseFragment
import com.yolo.fun_habit_journal.framework.presentation.common.hideKeyboard
import com.yolo.fun_habit_journal.framework.presentation.habitdetail.HABIT_DETAIL_SELECTED_HABIT_BUNDLE_KEY
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListStateEvent
import com.yolo.fun_habit_journal.framework.presentation.habitlist.state.HabitListViewState
import kotlinx.android.synthetic.main.fragment_habit_list.add_new_habit_fab
import kotlinx.android.synthetic.main.fragment_habit_list.recycler_view
import kotlinx.android.synthetic.main.fragment_habit_list.swipe_refresh
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

const val HABIT_LIST_STATE_BUNDLE_KEY = "com.yolo.fun_habit_journal.framework.presentation.habitlist.state"

@ExperimentalCoroutinesApi
@FlowPreview
class HabitListFragment constructor(
    private val viewModelFactory: ViewModelProvider.Factory,
    private val dateUtil: DateUtil
) : BaseFragment(R.layout.fragment_habit_list), HabitListAdapter.Interaction {

    val viewModel: HabitListViewModel by viewModels {
        viewModelFactory
    }

    private var listAdapter: HabitListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.setupDataStateManager()
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
        viewModel.viewState.observe(viewLifecycleOwner, Observer { viewState ->
            if (viewState != null) {
                viewState.habitList?.let { habitList ->
                    listAdapter?.submitList(habitList)
                    listAdapter?.notifyDataSetChanged()
                }

                viewState.newHabit?.let { newHabit ->
                    navigateToDetailFragment(newHabit)
                }
            }
        })

        viewModel.shouldDisplayProgressBar.observe(viewLifecycleOwner, Observer {
//            printActiveJobs()
            uiController.displayProgressBar(it)
        })

        viewModel.stateMessage.observe(viewLifecycleOwner, Observer { stateMessage ->
            stateMessage?.let { message ->
                    uiController.onResponseReceived(
                        response = message.response,
                        stateMessageCallback = object : StateMessageCallback {
                            override fun removeMessageFromStack() {
                                viewModel.clearStateMessage()
                            }
                        }
                    )
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

            listAdapter = HabitListAdapter(
                this@HabitListFragment,
                viewLifecycleOwner,
                dateUtil
            )

            adapter = listAdapter
        }

        viewModel.setStateEvent(HabitListStateEvent.GetHabitsLisEvent)
    }

    private fun setupSwipeRefresh() =
        swipe_refresh.setOnRefreshListener {
            swipe_refresh.isRefreshing = false
        }

    private fun setupFAB() =
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

    override fun inject() {}

    override fun onResume() {
        super.onResume()
        viewModel.clearList()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val viewState = viewModel.viewState.value

        outState.putParcelable(
            HABIT_LIST_STATE_BUNDLE_KEY,
            viewState
        )
        super.onSaveInstanceState(outState)
    }

    override fun onItemSelected(position: Int, item: Habit) {
        viewModel.setHabit(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listAdapter = null // can leak memory
    }
}
