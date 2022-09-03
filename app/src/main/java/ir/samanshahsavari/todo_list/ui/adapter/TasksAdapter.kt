package ir.samanshahsavari.todo_list.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ir.samanshahsavari.todo_list.ui.listener.OnItemClickListener
import ir.samanshahsavari.todo_list.data.db.model.Task
import ir.samanshahsavari.todo_list.databinding.TaskItemBinding

class TasksAdapter(private val onItemClickListener: OnItemClickListener) : ListAdapter<Task, TasksAdapter.TasksViewHolder>(
    DiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val binding = TaskItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.bind(currentItem)
    }

    inner class TasksViewHolder(private val binding: TaskItemBinding) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.apply {
                root.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        onItemClickListener.onItemClick(task)
                    }
                }
                checkbox.setOnClickListener {
                    val position = adapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        val task = getItem(position)
                        onItemClickListener.onCheckBoxClick(task, checkbox.isChecked)
                    }
                }
            }
        }

        fun bind(task: Task) {
            binding.apply {
                checkbox.isChecked = task.completed
                taskTitle.text = task.name
                taskTitle.paint.isStrikeThruText = task.completed
                priority.isVisible = task.important
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<Task>() {

        override fun areItemsTheSame(oldItem: Task, newItem: Task) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: Task, newItem: Task) = oldItem == newItem

    }
}