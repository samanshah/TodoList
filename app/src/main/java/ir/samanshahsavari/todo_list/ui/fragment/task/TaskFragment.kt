package ir.samanshahsavari.todo_list.ui.fragment.task

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ir.samanshahsavari.todo_list.R
import ir.samanshahsavari.todo_list.viewmodel.TaskViewModel
import ir.samanshahsavari.todo_list.databinding.FragmentTaskBinding
import ir.samanshahsavari.todo_list.utils.Constants
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class TaskFragment : Fragment(R.layout.fragment_task) {
    private var _binding: FragmentTaskBinding? = null
    private val binding get() = _binding!!
    private val taskViewModel by viewModels<TaskViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskBinding.inflate(inflater, container, false)

        binding.apply {
            taskName.editText?.setText(taskViewModel.taskName)
            taskDescription.editText?.setText(taskViewModel.taskDescription)
            importantCheckbox.isChecked = taskViewModel.taskImportance
            importantCheckbox.jumpDrawablesToCurrentState()
            dateCreated.isVisible = taskViewModel.task != null
            val dateCreatedText = "${getString(R.string.date_created)}: ${taskViewModel.task?.createdDateFormatted}"
            dateCreated.text = dateCreatedText

            taskName.editText?.addTextChangedListener {
                taskViewModel.taskName = it.toString()
            }

            taskDescription.editText?.addTextChangedListener {
                taskViewModel.taskDescription = it.toString()
            }

            importantCheckbox.setOnCheckedChangeListener { _, isChecked ->
                taskViewModel.taskImportance = isChecked
            }

            saveTask.setOnClickListener {
                taskViewModel.onSaveClick()
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            taskViewModel.addEditTaskEvent.collect { event ->
                when (event) {
                    is TaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage -> {
                        Snackbar.make(requireView(), event.message, Snackbar.LENGTH_LONG).show()
                    }
                    is TaskViewModel.AddEditTaskEvent.NavigateBackWithResult -> {
                        binding.taskName.clearFocus()
                        setFragmentResult(
                            Constants.FRAGMENT_ADD_EDIT_REQUEST,
                            bundleOf(Constants.FRAGMENT_ADD_EDIT_RESULT to event.result)
                        )
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }
}