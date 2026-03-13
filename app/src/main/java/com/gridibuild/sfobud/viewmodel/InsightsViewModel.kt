package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.*
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

data class InsightsSummary(
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val overdueTasks: Int = 0,
    val totalExpenses: Double = 0.0,
    val totalBudget: Double = 0.0,
    val expenseByCategory: Map<String, Double> = emptyMap(),
    val rooms: List<RoomEntity> = emptyList(),
    val roomExpenses: Map<Long, Double> = emptyMap(),
    val mostExpensiveRoom: RoomEntity? = null,
    val completionPercent: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class InsightsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    private val project: Flow<ProjectEntity?> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getProjectByIdFlow(id) else flowOf(null)
    }

    private val tasks: Flow<List<TaskEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getTasksByProject(id) else flowOf(emptyList())
    }

    private val expenses: Flow<List<BudgetExpenseEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getExpensesByProject(id) else flowOf(emptyList())
    }

    private val rooms: Flow<List<RoomEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getRoomsByProject(id) else flowOf(emptyList())
    }

    val summary: StateFlow<InsightsSummary> = combine(project, tasks, expenses, rooms) { proj, taskList, expList, roomList ->
        val now = System.currentTimeMillis()
        val totalTasks = taskList.size
        val completedTasks = taskList.count { it.status == "DONE" }
        val overdueTasks = taskList.count { it.dueDate != null && it.dueDate < now && it.status != "DONE" }
        val totalSpent = expList.filter { !it.isPlanned }.sumOf { it.amount }
        val expByCat = expList.filter { !it.isPlanned }.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
        val completionPercent = if (totalTasks > 0) (completedTasks * 100 / totalTasks) else 0
        InsightsSummary(
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            overdueTasks = overdueTasks,
            totalExpenses = totalSpent,
            totalBudget = proj?.totalBudget ?: 0.0,
            expenseByCategory = expByCat,
            rooms = roomList,
            completionPercent = completionPercent
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, InsightsSummary())
}
