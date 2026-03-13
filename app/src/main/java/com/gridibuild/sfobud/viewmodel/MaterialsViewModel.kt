package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.MaterialEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class MaterialsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    private val _categoryFilter = MutableStateFlow("All")
    val categoryFilter: StateFlow<String> = _categoryFilter

    private val allMaterials: StateFlow<List<MaterialEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getMaterialsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val materials: StateFlow<List<MaterialEntity>> = combine(allMaterials, _categoryFilter) { mats, cat ->
        if (cat == "All") mats else mats.filter { it.category == cat }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val totalCost: StateFlow<Double> = allMaterials.map { list ->
        list.sumOf { it.quantity * it.pricePerUnit }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0.0)

    val categories = listOf("All", "Paint", "Flooring", "Tiles", "Lighting", "Plumbing", "Hardware", "Decor", "Tools", "Electrical", "Other")

    fun setCategoryFilter(cat: String) { _categoryFilter.value = cat }

    fun addMaterial(name: String, category: String, quantity: Double, unit: String, pricePerUnit: Double, notes: String, roomId: Long?) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.insertMaterial(
                MaterialEntity(projectId = projectId, roomId = roomId, name = name.trim(), category = category, quantity = quantity, unit = unit, pricePerUnit = pricePerUnit, notes = notes)
            )
        }
    }

    fun updateMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.updateMaterial(material) }
    }

    fun togglePurchased(material: MaterialEntity) {
        viewModelScope.launch { repository.updateMaterial(material.copy(purchased = !material.purchased)) }
    }

    fun deleteMaterial(material: MaterialEntity) {
        viewModelScope.launch { repository.deleteMaterial(material) }
    }
}
