package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.ProjectEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ProjectViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentUserId: StateFlow<Long> = prefs.currentUserId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val projects: StateFlow<List<ProjectEntity>> = currentUserId.flatMapLatest { userId ->
        if (userId > 0) repository.getProjectsByUser(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val archivedProjects: StateFlow<List<ProjectEntity>> = currentUserId.flatMapLatest { userId ->
        if (userId > 0) repository.getArchivedProjectsByUser(userId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val currentProject: StateFlow<ProjectEntity?> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getProjectByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun createProject(name: String, description: String, address: String, budget: Double, startDate: Long?, endDate: Long?) {
        val userId = currentUserId.value
        if (userId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            val id = repository.insertProject(
                ProjectEntity(userId = userId, name = name.trim(), description = description, address = address, totalBudget = budget, startDate = startDate, endDate = endDate)
            )
            prefs.setCurrentProjectId(id)
        }
    }

    fun updateProject(project: ProjectEntity) {
        viewModelScope.launch { repository.updateProject(project) }
    }

    fun deleteProject(project: ProjectEntity) {
        viewModelScope.launch {
            repository.deleteProject(project)
            if (currentProjectId.value == project.id) prefs.setCurrentProjectId(-1L)
        }
    }

    fun archiveProject(project: ProjectEntity) {
        viewModelScope.launch { repository.updateProject(project.copy(isArchived = true, isActive = false)) }
    }

    fun unarchiveProject(project: ProjectEntity) {
        viewModelScope.launch { repository.updateProject(project.copy(isArchived = false, isActive = true)) }
    }

    fun selectProject(projectId: Long) {
        viewModelScope.launch { prefs.setCurrentProjectId(projectId) }
    }

    fun clearError() { _error.value = null }
}
