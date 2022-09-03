package ir.samanshahsavari.todo_list.ui.event

import ir.samanshahsavari.todo_list.data.db.model.Task

sealed class TasksEvent {
    object NavigateToAddTaskFragment : TasksEvent()
    data class NavigateToEditTaskFragment(val task: Task) : TasksEvent()
    data class ShowUndoDeleteTaskToastMessage(val task: Task) : TasksEvent()
    data class ShowTaskSavedConfigurationMessage(val text: String): TasksEvent()
    object ShowDeleteAllCompletedScreen : TasksEvent()
}