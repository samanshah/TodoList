package ir.samanshahsavari.todo_list.data.db.config

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import ir.samanshahsavari.todo_list.data.db.model.Task
import ir.samanshahsavari.todo_list.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider

@Database(entities = [Task::class], version = 2)
abstract class TaskDatabase : RoomDatabase() {

    abstract fun taskDao() : TaskDao

    class Callback @Inject constructor(
        private val database: Provider<TaskDatabase>,
        @ApplicationScope private val applicationScope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)

            val dao = database.get().taskDao()

            applicationScope.launch {
                dao.insert(Task("Task 1", important = true))
                dao.insert(Task("Task 2", important = true))
                dao.insert(Task("Task 3"))
                dao.insert(Task("Task 4", completed = true))
                dao.insert(Task("Task 5"))
                dao.insert(Task("Task 6"))
                dao.insert(Task("Task 7", completed = true, important = true, description = "Task 7 description."))
            }
        }
    }
}