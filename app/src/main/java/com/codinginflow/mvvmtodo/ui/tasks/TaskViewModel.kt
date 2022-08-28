package com.codinginflow.mvvmtodo.ui.tasks


import androidx.hilt.Assisted
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.*
import com.codinginflow.mvvmtodo.data.PreferencesManager
import com.codinginflow.mvvmtodo.data.SortOrder
import com.codinginflow.mvvmtodo.data.Task
import com.codinginflow.mvvmtodo.data.TaskDao
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch


class TaskViewModel @ViewModelInject constructor(
    private val taskDao: TaskDao,
    private val preferencesManager: PreferencesManager,
    @Assisted private val state : SavedStateHandle
) : ViewModel() {
//    val searchQuery = MutableStateFlow("")

    val searchQuery = state.getLiveData("searchQuery","")

    val preferencesFlow = preferencesManager.preferencesFlow

    private val taskEventChannel = Channel<TaskEvent>()
    val taskEvent = taskEventChannel.receiveAsFlow()

    private val taskFlow = combine(searchQuery.asFlow(), preferencesFlow) { query, filterPreferences ->
        Pair(query, filterPreferences)
    }.flatMapLatest { (query, filterPreferences) -> //using object decomposition
        taskDao.getTask(query, filterPreferences.sortOrder, filterPreferences.hideCompleted)
    }
    val tasks = taskFlow.asLiveData()

    fun onSortOrderSelected(sortOrder: SortOrder) = viewModelScope.launch {
        preferencesManager.updateSortOrder(sortOrder)
    }

    fun onHideCompletedSelected(hideCompleted: Boolean) = viewModelScope.launch {
        preferencesManager.updateHideCompleted(hideCompleted)
    }


    fun onTaskSelected(task: Task) {

    }

    fun onTaskCheckedChanged(task: Task, checked: Boolean) = viewModelScope.launch {
        taskDao.update(task.copy(completed = checked))
    }

    fun onTaskSwiped(task: Task)= viewModelScope.launch {
        taskDao.delete(task)
        taskEventChannel.send(TaskEvent.showUndoDeleteTaskMessage(task))
    }

    fun onUndoDeleteClick(task: Task)= viewModelScope.launch {
        taskDao.insert(task)

    }

    sealed class TaskEvent{
        data class showUndoDeleteTaskMessage(val Task: Task): TaskEvent()
    }
}

