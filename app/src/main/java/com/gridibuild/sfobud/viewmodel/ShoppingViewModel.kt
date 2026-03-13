package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.ShoppingItemEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    private val _tabFilter = MutableStateFlow("Urgent")
    val tabFilter: StateFlow<String> = _tabFilter

    private val allItems: StateFlow<List<ShoppingItemEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getShoppingItemsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val items: StateFlow<List<ShoppingItemEntity>> = combine(allItems, _tabFilter) { items, tab ->
        when (tab) {
            "Urgent" -> items.filter { it.urgency == "URGENT" && it.status == "PENDING" }
            "This Week" -> items.filter { it.urgency == "THIS_WEEK" && it.status == "PENDING" }
            "Later" -> items.filter { it.urgency == "LATER" && it.status == "PENDING" }
            "Bought" -> items.filter { it.status == "BOUGHT" }
            else -> items
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val tabs = listOf("Urgent", "This Week", "Later", "Bought")

    fun setTab(tab: String) { _tabFilter.value = tab }

    fun addItem(name: String, category: String, quantity: Double, unit: String, estimatedPrice: Double, urgency: String, notes: String) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.insertShoppingItem(
                ShoppingItemEntity(projectId = projectId, name = name.trim(), category = category, quantity = quantity, unit = unit, estimatedPrice = estimatedPrice, urgency = urgency, notes = notes)
            )
        }
    }

    fun markBought(item: ShoppingItemEntity, actualPrice: Double? = null) {
        viewModelScope.launch {
            repository.updateShoppingItem(item.copy(status = "BOUGHT", actualPrice = actualPrice ?: item.estimatedPrice))
        }
    }

    fun markPending(item: ShoppingItemEntity) {
        viewModelScope.launch { repository.updateShoppingItem(item.copy(status = "PENDING")) }
    }

    fun updateItem(item: ShoppingItemEntity) {
        viewModelScope.launch { repository.updateShoppingItem(item) }
    }

    fun deleteItem(item: ShoppingItemEntity) {
        viewModelScope.launch { repository.deleteShoppingItem(item) }
    }
}
