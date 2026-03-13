package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.RoomEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class RoomsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val rooms: StateFlow<List<RoomEntity>> = currentProjectId.flatMapLatest { projectId ->
        if (projectId > 0) repository.getRoomsByProject(projectId) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedRoomId = MutableStateFlow<Long>(-1L)

    val selectedRoom: StateFlow<RoomEntity?> = _selectedRoomId.flatMapLatest { id ->
        if (id > 0) repository.getRoomByIdFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val roomColors = listOf("#FFC83A", "#FF9F43", "#FF6B57", "#3B82F6", "#35D0BA", "#52C873", "#9B59B6", "#E74C3C")
    val roomStages = listOf("Planning", "Preparing", "In Progress", "Finishing", "Done")

    fun selectRoom(roomId: Long) { _selectedRoomId.value = roomId }

    fun createRoom(name: String, type: String, width: Double, height: Double, length: Double, ceilingHeight: Double, colorHex: String) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.insertRoom(
                RoomEntity(projectId = projectId, name = name.trim(), type = type, width = width, height = height, length = length, ceilingHeight = ceilingHeight, colorHex = colorHex)
            )
        }
    }

    fun updateRoom(room: RoomEntity) {
        viewModelScope.launch { repository.updateRoom(room) }
    }

    fun updateRoomStage(room: RoomEntity, stage: String) {
        viewModelScope.launch { repository.updateRoom(room.copy(stage = stage)) }
    }

    fun deleteRoom(room: RoomEntity) {
        viewModelScope.launch { repository.deleteRoom(room) }
    }
}
