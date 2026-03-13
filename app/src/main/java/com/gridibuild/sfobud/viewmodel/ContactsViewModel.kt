package com.gridibuild.sfobud.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.local.entity.ContactEntity
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(AppDatabase.getInstance(application))
    private val prefs = UserPreferences(application)

    val currentProjectId: StateFlow<Long> = prefs.currentProjectId
        .stateIn(viewModelScope, SharingStarted.Eagerly, -1L)

    val contacts: StateFlow<List<ContactEntity>> = currentProjectId.flatMapLatest { id ->
        if (id > 0) repository.getContactsByProject(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val roles = listOf("Master", "Electrician", "Plumber", "Carpenter", "Painter", "Delivery", "Store", "Designer", "Other")

    fun addContact(name: String, role: String, phone: String, email: String, company: String, notes: String) {
        val projectId = currentProjectId.value
        if (projectId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            repository.insertContact(
                ContactEntity(projectId = projectId, name = name.trim(), role = role, phone = phone.trim(), email = email.trim(), company = company.trim(), notes = notes)
            )
        }
    }

    fun updateContact(contact: ContactEntity) {
        viewModelScope.launch { repository.updateContact(contact) }
    }

    fun deleteContact(contact: ContactEntity) {
        viewModelScope.launch { repository.deleteContact(contact) }
    }
}
