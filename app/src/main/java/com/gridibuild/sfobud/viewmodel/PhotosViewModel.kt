package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.PhotoEntity
import com.gridibuild.sfobud.data.local.entity.RoomEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class PhotosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val photos: StateFlow<List<PhotoEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getPhotosByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val rooms: StateFlow<List<RoomEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getRoomsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _stageFilter = MutableStateFlow("ALL")
    val stageFilter: StateFlow<String> = _stageFilter

    val filteredPhotos: StateFlow<List<PhotoEntity>> = combine(photos, _stageFilter) { photoList, stage ->
        if (stage == "ALL") photoList else photoList.filter { it.stage == stage }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val stages = listOf("ALL", "BEFORE", "DURING", "AFTER")

    fun setStageFilter(stage: String) { _stageFilter.value = stage }

    fun addPhoto(imagePath: String, stage: String, caption: String, roomId: Long?) {
        val projectId = currentProjectId.value
        if (projectId <= 0) return
        viewModelScope.launch {
            repository.insertPhoto(
                PhotoEntity(projectId = projectId, roomId = roomId, stage = stage, imagePath = imagePath, caption = caption)
            )
        }
    }

    fun deletePhoto(photo: PhotoEntity) {
        viewModelScope.launch { repository.deletePhoto(photo) }
    }

    fun updateCaption(photo: PhotoEntity, caption: String) {
        viewModelScope.launch { repository.updatePhoto(photo.copy(caption = caption)) }
    }
}
