package ir.samanshahsavari.todo_list.data.repository

import ir.samanshahsavari.todo_list.data.db.config.SortOrder
import ir.samanshahsavari.todo_list.data.db.config.TaskDao
import ir.samanshahsavari.todo_list.data.db.model.Task
import ir.samanshahsavari.todo_list.utils.PreferencesManager
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class TasksRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager
) {

    suspend fun onSortOrderSelected(sortOrder: SortOrder) {
        preferencesManager.updateSortOrder(sortOrder)
    }

    suspend fun onHideCompletedSelected(hideCompleted: Boolean) {
        preferencesManager.updateHideCompleted(hideCompleted)
    }

    val preferencesFlow = preferencesManager.preferencesFlow

    suspend fun getHideCompleted() = preferencesFlow.first().hideCompleted

    fun getTasks(searchQuery: String, sortOrder: SortOrder, hideCompleted: Boolean) = taskDao.getTasks(searchQuery, sortOrder, hideCompleted)

    suspend fun insert(task: Task) {
        taskDao.insert(task)
    }

    suspend fun updateTask(task: Task, isChecked: Boolean) {
        taskDao.update(task.copy(completed = isChecked))
    }

    suspend fun deleteTask(task: Task) {
        taskDao.delete(task)
    }

    suspend fun deleteCompletedTasks() {
        taskDao.deleteCompletedTasks()
    }
}