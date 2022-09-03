package ir.samanshahsavari.todo_list.viewmodel

import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import ir.samanshahsavari.todo_list.data.db.config.SortOrder
import ir.samanshahsavari.todo_list.data.db.model.Task
import ir.samanshahsavari.todo_list.ui.event.TasksEvent
import ir.samanshahsavari.todo_list.data.repository.TasksRepository
import ir.samanshahsavari.todo_list.di.ApplicationScope
import ir.samanshahsavari.todo_list.ui.activity.ADD_TASK_RESULT_OK
import ir.samanshahsavari.todo_list.ui.activity.EDIT_TASK_RESULT_OK
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TasksViewModel @ViewModelInject constructor(
    private val tasksRepository: TasksRepository,
    @Assisted private val state: SavedStateHandle,
    @ApplicationScope private val applicationScope: CoroutineScope
) : ViewModel() {

    val searchQuery = state.getLiveData("searchQuery", "")

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private val tasksFlow = combine(
        searchQuery.asFlow(),
        tasksRepository.preferencesFlow
    ) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) ->
        tasksRepository.getTasks(
            query,
            filterPreferences.sortOrder,
            filterPreferences.hideCompleted
        )
    }

    val tasks = tasksFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        tasksRepository.onSortOrderSelected(sortOrder)
    }

    fun onHideCompletedSelected(hideCompleted: Boolean) = viewModelScope.launch {
        tasksRepository.onHideCompletedSelected(hideCompleted)
    }

    suspend fun getHideCompleted() = tasksRepository.getHideCompleted()

    fun insert(task: Task) = viewModelScope.launch {
        tasksRepository.insert(task)
    }

    fun onTaskSelected(task: Task) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToEditTaskFragment(task))
    }

    fun onTaskCheckedChanged(task: Task, isChecked: Boolean) = viewModelScope.launch {
        tasksRepository.updateTask(task, isChecked)
    }

    fun onTaskSwiped(task: Task) = viewModelScope.launch {
        tasksRepository.deleteTask(task)
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskToastMessage(task))
    }

    fun onUndoDeleteClick(task: Task) = viewModelScope.launch {
        insert(task)
    }

    fun onAddNewTaskClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.NavigateToAddTaskFragment)
    }

    fun onAddEditResult(result: Int) {
        when(result) {
            ADD_TASK_RESULT_OK -> showTaskSavedConfigurationMessage("Task added")
            EDIT_TASK_RESULT_OK -> showTaskSavedConfigurationMessage("Task updated")
        }
    }

    private fun showTaskSavedConfigurationMessage(text: String) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowTaskSavedConfigurationMessage(text))
    }

    fun onDeleteAllCompletedClick() = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowDeleteAllCompletedScreen)
    }

    fun onConfirmClick() = applicationScope.launch {
        tasksRepository.deleteCompletedTasks()
    }
}