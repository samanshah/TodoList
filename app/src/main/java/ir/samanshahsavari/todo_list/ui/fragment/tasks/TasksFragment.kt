package ir.samanshahsavari.todo_list.ui.fragment.tasks

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.samanshahsavari.todo_list.R
import ir.samanshahsavari.todo_list.data.db.config.SortOrder
import ir.samanshahsavari.todo_list.data.db.model.Task
import ir.samanshahsavari.todo_list.databinding.FragmentTasksBinding
import ir.samanshahsavari.todo_list.ui.adapter.TasksAdapter
import ir.samanshahsavari.todo_list.ui.event.TasksEvent
import ir.samanshahsavari.todo_list.ui.listener.OnItemClickListener
import ir.samanshahsavari.todo_list.utils.Constants
import ir.samanshahsavari.todo_list.utils.exhaustive
import ir.samanshahsavari.todo_list.utils.onQueryTextChanged
import ir.samanshahsavari.todo_list.viewmodel.TasksViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TasksFragment : Fragment(R.layout.fragment_tasks), OnItemClickListener {
    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!
    private val tasksViewModel by viewModels<TasksViewModel>()
    private val tasksAdapter = TasksAdapter(this)
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)

        binding.apply {
            tasksList.apply {
                adapter = tasksAdapter
                layoutManager = LinearLayoutManager(requireContext())
                setHasFixedSize(true)
            }

            ItemTouchHelper(object :
                ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {
                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder
                ): Boolean {
                    return false
                }

                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                    val task = tasksAdapter.currentList[viewHolder.adapterPosition]
                    tasksViewModel.onTaskSwiped(task)
                }

            }).attachToRecyclerView(tasksList)

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                tasksViewModel.tasksEvent.collect { event ->
                    when (event) {
                        is TasksEvent.ShowUndoDeleteTaskToastMessage -> {
                            Snackbar.make(
                                requireView(),
                                getString(R.string.deleted_successfully),
                                Snackbar.LENGTH_LONG
                            )
                                .setAction(getString(R.string.undo)) { tasksViewModel.onUndoDeleteClick(event.task) }
                                .show()
                        }
                        is TasksEvent.NavigateToAddTaskFragment -> {
                            val action =
                                TasksFragmentDirections.actionTasksFragmentToTaskFragment(title = getString(R.string.new_task))
                            findNavController().navigate(action)
                        }
                        is TasksEvent.NavigateToEditTaskFragment -> {
                            val action = TasksFragmentDirections.actionTasksFragmentToTaskFragment(
                                task = event.task,
                                title = getString(R.string.edit_task)
                            )
                            findNavController().navigate(action)
                        }
                        is TasksEvent.ShowTaskSavedConfigurationMessage -> {
                            Snackbar.make(requireView(), event.text, Snackbar.LENGTH_SHORT).show()
                        }
                        is TasksEvent.ShowDeleteAllCompletedScreen -> {
                            AlertDialog.Builder(requireContext())
                                .setTitle(getString(R.string.delete_confirmation))
                                .setMessage(getString(R.string.do_you_want_to_delete_all_tasks))
                                .setPositiveButton(getString(R.string.yes)) { _, _ ->
                                    tasksViewModel.onConfirmClick()
                                }
                                .setNegativeButton(getString(R.string.cancel), null)
                                .create()
                                .show()
                        }
                    }.exhaustive
                }
            }

            addNewTask.setOnClickListener {
                tasksViewModel.onAddNewTaskClick()
            }

            setHasOptionsMenu(true)
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tasksViewModel.tasks.observe(viewLifecycleOwner) { tasks ->
            tasksAdapter.submitList(tasks)
        }

        setFragmentResultListener(Constants.FRAGMENT_ADD_EDIT_REQUEST) { _, bundle ->
            val result = bundle.getInt(Constants.FRAGMENT_ADD_EDIT_RESULT)
            tasksViewModel.onAddEditResult(result)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)

        val searchItem = menu.findItem(R.id.search)
        searchView = searchItem.actionView as SearchView

        // for rotate device
        val pendingQuery = tasksViewModel.searchQuery.value
        if (pendingQuery != null && pendingQuery.isNotEmpty()) {
            searchItem.expandActionView()
            searchView.setQuery(pendingQuery, false)
        }

        searchView.onQueryTextChanged {
            tasksViewModel.searchQuery.value = it
        }

        viewLifecycleOwner.lifecycleScope.launch {
            menu.findItem(R.id.hide_completed).isChecked = tasksViewModel.getHideCompleted()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_by_name -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_NAME)
                true
            }
            R.id.sort_by_date -> {
                tasksViewModel.onSortOrderSelected(SortOrder.BY_DATE)
                true
            }
            R.id.hide_completed -> {
                item.isChecked = !item.isChecked
                tasksViewModel.onHideCompletedSelected(item.isChecked)
                true
            }
            R.id.delete_all -> {
                tasksViewModel.onDeleteAllCompletedClick()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onItemClick(task: Task) {
        tasksViewModel.onTaskSelected(task)
    }

    override fun onCheckBoxClick(task: Task, isChecked: Boolean) {
        tasksViewModel.onTaskCheckedChanged(task, isChecked)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchView.setOnQueryTextListener(null)
    }
}