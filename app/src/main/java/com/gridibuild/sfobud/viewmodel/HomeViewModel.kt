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

data class HomeState(
    val userName: String = "",
    val currentProject: ProjectEntity? = null,
    val rooms: List<RoomEntity> = emptyList(),
    val recentTasks: List<TaskEntity> = emptyList(),
    val totalTasks: Int = 0,
    val completedTasks: Int = 0,
    val totalSpent: Double = 0.0,
    val totalBudget: Double = 0.0,
    val pendingShoppingItems: Int = 0
)

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentUserId: StateFlow<Long> = prefs.currentUserId.stateIn(viewModelScope, SharingStarted.Eagerly, -1L)
    val currentProjectId: StateFlow<Long> = prefs.currentProjectId.stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val currentProject: StateFlow<ProjectEntity?> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getProjectByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val rooms: StateFlow<List<RoomEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getRoomsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tasks: StateFlow<List<TaskEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getTasksByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val expenses: StateFlow<List<BudgetExpenseEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getExpensesByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val shoppingItems: StateFlow<List<ShoppingItemEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getPendingShoppingItems(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
}
