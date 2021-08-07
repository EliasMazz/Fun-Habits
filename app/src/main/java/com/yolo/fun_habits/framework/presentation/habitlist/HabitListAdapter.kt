package com.yolo.fun_habits.framework.presentation.habitlist

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.library.baseAdapters.BR
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.yolo.fun_habits.R
import com.yolo.fun_habits.business.domain.model.Habit
import com.yolo.fun_habits.business.domain.util.DateUtil
import com.yolo.fun_habits.databinding.LayoutHabitListItemBinding

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

        val layoutInflater = LayoutInflater.from(parent.context)

        val binding: LayoutHabitListItemBinding = DataBindingUtil.inflate(layoutInflater, R.layout.layout_habit_list_item, parent, false)

        return HabitViewHolder(
            binding,
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

    class HabitViewHolder constructor(
        private val binding: LayoutHabitListItemBinding,
        private val interaction: Interaction?,
        private val lifecycleOwner: LifecycleOwner,
        private val dateUtil: DateUtil
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Habit) {
            binding.setVariable(BR.item, item)
            binding.executePendingBindings()
            binding.container.setOnClickListener {
                interaction?.onItemSelected(adapterPosition, item)
            }
        }
    }
    interface Interaction {
        fun onItemSelected(position: Int, item: Habit)
    }
}

