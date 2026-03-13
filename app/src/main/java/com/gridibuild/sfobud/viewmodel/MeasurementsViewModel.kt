package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.MeasurementEntity
import com.gridibuild.sfobud.data.local.entity.RoomEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MeasurementsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val rooms: StateFlow<List<RoomEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getRoomsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _selectedRoomId = MutableStateFlow<Long>(-1L)
    val selectedRoomId: StateFlow<Long> = _selectedRoomId

    val selectedRoom: StateFlow<RoomEntity?> = combine(rooms, _selectedRoomId) { rooms, id ->
        rooms.firstOrNull { it.id == id } ?: rooms.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val measurements: StateFlow<List<MeasurementEntity>> = selectedRoom.flatMapLatest { room ->
        if (room != null) repository.getMeasurementsByRoom(room.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val measurementTypes = listOf("Wall", "Ceiling", "Floor", "Window", "Door", "Other")
    val units = listOf("m", "cm", "mm", "ft", "in")

    fun selectRoom(roomId: Long) { _selectedRoomId.value = roomId }

    fun addMeasurement(type: String, name: String, value: Double, unit: String, notes: String) {
        val roomId = selectedRoom.value?.id ?: return
        viewModelScope.launch {
            repository.insertMeasurement(
                MeasurementEntity(roomId = roomId, type = type, name = name, value = value, unit = unit, notes = notes)
            )
        }
    }

    fun updateMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch { repository.updateMeasurement(measurement) }
    }

    fun deleteMeasurement(measurement: MeasurementEntity) {
        viewModelScope.launch { repository.deleteMeasurement(measurement) }
    }
}
