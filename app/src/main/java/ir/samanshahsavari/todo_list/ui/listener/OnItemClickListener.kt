package ir.samanshahsavari.todo_list.ui.listener

import ir.samanshahsavari.todo_list.data.db.model.Task

interface OnItemClickListener {

    fun onItemClick(task: Task)

    fun onCheckBoxClick(task: Task, isChecked: Boolean)
}