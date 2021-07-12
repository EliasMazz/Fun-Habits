package com.yolo.fun_habit_journal.framework.presentation.habitlist

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yolo.fun_habit_journal.R
import com.yolo.fun_habit_journal.business.domain.model.Habit
import com.yolo.fun_habit_journal.business.domain.util.DateUtil
import com.yolo.fun_habit_journal.framework.util.printLogD
import kotlinx.android.synthetic.main.layout_habit_list_item.view.habit_timestamp
import kotlinx.android.synthetic.main.layout_habit_list_item.view.habit_title

class HabitListAdapter(
    private val interaction: Interaction? = null,
    private val lifecycleOwner: LifecycleOwner,
    private val dateUtil: DateUtil
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Habit>() {

        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, DIFF_CALLBACK)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return HabitViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.layout_habit_list_item,
                parent,
                false
            ),
            interaction,
            lifecycleOwner,
            dateUtil
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HabitViewHolder -> {
                holder.bind(differ.currentList.get(position))
            }
        }
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Habit>) {
        differ.submitList(list)
    }

    class HabitViewHolder
    constructor(
        itemView: View,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(itemView) {

        private lateinit var habit: Habit

        fun bind(item: Habit) = with(itemView) {
            habit = item
            habit_title.text = item.title
            habit_timestamp.text = dateUtil.removeTimeFromDateString(item.updated_at)

            setOnClickListener {
                interaction?.onItemSelected(adapterPosition, habit)
            }
        }
    }

    interface Interaction {
        fun onItemSelected(position: Int, item: Habit)
    }
}

