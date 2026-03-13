package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.TaskEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class TaskFilter { ALL, TODAY, OVERDUE, IN_PROGRESS, COMPLETED }

@OptIn(ExperimentalCoroutinesApi::class)
class TasksViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    private val _filter = MutableStateFlow(TaskFilter.ALL)
    val filter: StateFlow<TaskFilter> = _filter

    private val allTasks: StateFlow<List<TaskEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getTasksByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tasks: StateFlow<List<TaskEntity>> = combine(allTasks, _filter) { taskList, currentFilter ->
        val now = System.currentTimeMillis()
        when (currentFilter) {
            TaskFilter.ALL -> taskList
            TaskFilter.TODAY -> taskList.filter { it.dueDate != null && isSameDay(it.dueDate, now) }
            TaskFilter.OVERDUE -> taskList.filter { it.dueDate != null && it.dueDate < now && it.status != "DONE" }
            TaskFilter.IN_PROGRESS -> taskList.filter { it.status == "IN_PROGRESS" }
            TaskFilter.COMPLETED -> taskList.filter { it.status == "DONE" }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun setFilter(f: TaskFilter) { _filter.value = f }

    fun createTask(title: String, description: String, priority: String, dueDate: Long?, roomId: Long?) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || title.isBlank()) return
        viewModelScope.launch {
            repository.insertTask(
                TaskEntity(projectId = projectId, roomId = roomId, title = title.trim(), description = description, priority = priority, dueDate = dueDate)
            )
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch { repository.updateTask(task) }
    }

    fun toggleTaskStatus(task: TaskEntity) {
        viewModelScope.launch {
            val newStatus = when (task.status) {
                "TODO" -> "IN_PROGRESS"
                "IN_PROGRESS" -> "DONE"
                else -> "TODO"
            }
            repository.updateTask(task.copy(status = newStatus, completedAt = if (newStatus == "DONE") System.currentTimeMillis() else null))
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch { repository.deleteTask(task) }
    }

    private fun isSameDay(ts1: Long, ts2: Long): Boolean {
        val cal1 = java.util.Calendar.getInstance().also { it.timeInMillis = ts1 }
        val cal2 = java.util.Calendar.getInstance().also { it.timeInMillis = ts2 }
        return cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR) &&
                cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR)
    }
}
