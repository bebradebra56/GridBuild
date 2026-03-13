package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.BudgetExpenseEntity
import com.gridibuild.sfobud.data.local.entity.ProjectEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class BudgetSummary(
    val totalBudget: Double = 0.0,
    val totalSpent: Double = 0.0,
    val totalPlanned: Double = 0.0,
    val remaining: Double = 0.0,
    val isOverBudget: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val currentProject: StateFlow<ProjectEntity?> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getProjectByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val expenses: StateFlow<List<BudgetExpenseEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getExpensesByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val summary: StateFlow<BudgetSummary> = combine(currentProject, expenses) { project, expList ->
        val budget = project?.totalBudget ?: 0.0
        val spent = expList.filter { !it.isPlanned }.sumOf { it.amount }
        val planned = expList.filter { it.isPlanned }.sumOf { it.amount }
        BudgetSummary(
            totalBudget = budget,
            totalSpent = spent,
            totalPlanned = planned,
            remaining = budget - spent,
            isOverBudget = spent > budget && budget > 0
        )
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BudgetSummary())

    val expenseByCategory: StateFlow<Map<String, Double>> = expenses.map { list ->
        list.filter { !it.isPlanned }.groupBy { it.category }.mapValues { (_, v) -> v.sumOf { it.amount } }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val categories = listOf("Materials", "Labor", "Delivery", "Furniture", "Decor", "Tools", "Other")

    fun addExpense(description: String, amount: Double, category: String, isPlanned: Boolean, isUnexpected: Boolean) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || description.isBlank() || amount <= 0) return
        viewModelScope.launch {
            repository.insertExpense(
                BudgetExpenseEntity(projectId = projectId, description = description.trim(), amount = amount, category = category, isPlanned = isPlanned, isUnexpected = isUnexpected)
            )
        }
    }

    fun updateExpense(expense: BudgetExpenseEntity) {
        viewModelScope.launch { repository.updateExpense(expense) }
    }

    fun deleteExpense(expense: BudgetExpenseEntity) {
        viewModelScope.launch { repository.deleteExpense(expense) }
    }

    fun updateProjectBudget(newBudget: Double) {
        viewModelScope.launch {
            currentProject.value?.let { repository.updateProject(it.copy(totalBudget = newBudget)) }
        }
    }
}
