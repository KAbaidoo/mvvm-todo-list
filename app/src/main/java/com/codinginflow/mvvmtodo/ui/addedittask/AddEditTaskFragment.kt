package com.codinginflow.mvvmtodo.ui.addedittask

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.codinginflow.mvvmtodo.R
import com.codinginflow.mvvmtodo.databinding.FragmentAddEditTaskBinding
import com.codinginflow.mvvmtodo.util.exhaustive
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_edit_task.*
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class AddEditTaskFragment : Fragment(R.layout.fragment_add_edit_task) {
    private val viewModel:AddEditTaskViewModel by viewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val binding = FragmentAddEditTaskBinding.bind(view)

        binding.apply {
            editTextName.setText(viewModel.taskName)
            checkBoxImportant.isChecked = viewModel.taskImportant
            checkBoxImportant.jumpDrawablesToCurrentState()
            textViewDateCreated.isVisible = viewModel.task != null
            textViewDateCreated.text = "Created: ${viewModel.task?.createdDateFormatted}"

            editTextName.addTextChangedListener { 
                viewModel.taskName = it.toString()
            }
            
            checkBoxImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.taskImportant =isChecked
            }
            fabSaveTask.setOnClickListener {
                viewModel.onSaveClicked()
            }

            viewLifecycleOwner.lifecycleScope.launchWhenStarted {
                viewModel.addEditTaskEvent.collect {
                    event ->
                    when (event) {
                        is AddEditTaskViewModel.AddEditTaskEvent.ShowInvalidInputMessage ->
                        {
                            Snackbar.make(requireView(),event.msg,Snackbar.LENGTH_LONG).show()
                        }
                        is AddEditTaskViewModel.AddEditTaskEvent.NavigateBackWithResults -> {
                            binding.editTextName.clearFocus()
                            setFragmentResult(
                                "add_edit_request",
                                bundleOf("add_edit_result" to event.results)
                            )
                            findNavController().popBackStack()
                        }
                    }.exhaustive
                }
            }

        }

    }
}