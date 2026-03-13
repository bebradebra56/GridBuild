package com.gridibuild.sfobud.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.preferences.UserPreferences
import com.gridibuild.sfobud.data.repository.AppRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = UserPreferences(application)
    private val repository = AppRepository(AppDatabase.getInstance(application))

    val currency: StateFlow<String> = prefs.currency.stateIn(viewModelScope, SharingStarted.Eagerly, "USD")
    val units: StateFlow<String> = prefs.units.stateIn(viewModelScope, SharingStarted.Eagerly, "m")
    val isDarkTheme: StateFlow<Boolean> = prefs.isDarkTheme.stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val notificationsEnabled: StateFlow<Boolean> = prefs.notificationsEnabled.stateIn(viewModelScope, SharingStarted.Eagerly, true)

    private val _exportStatus = MutableStateFlow<ExportStatus>(ExportStatus.Idle)
    val exportStatus: StateFlow<ExportStatus> = _exportStatus

    val currencies = listOf("USD", "EUR", "GBP", "RUB", "UAH", "PLN", "CZK", "KZT")
    val unitOptions = listOf("m", "ft")

    fun setCurrency(currency: String) { viewModelScope.launch { prefs.setCurrency(currency) } }
    fun setUnits(units: String) { viewModelScope.launch { prefs.setUnits(units) } }
    fun setDarkTheme(isDark: Boolean) { viewModelScope.launch { prefs.setDarkTheme(isDark) } }
    fun setNotificationsEnabled(enabled: Boolean) { viewModelScope.launch { prefs.setNotificationsEnabled(enabled) } }
    fun clearExportStatus() { _exportStatus.value = ExportStatus.Idle }

    fun exportData(context: Context) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            try {
                val userId = prefs.currentUserId.first()
                val json = repository.exportAllDataAsJson(userId)
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val fileName = "gridbuild_export_$timestamp.json"
                val file = File(context.cacheDir, fileName)
                file.writeText(json)
                val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/json"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "GridBuild Data Export")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(intent, "Export Data").also {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                })
                _exportStatus.value = ExportStatus.Success("Data exported successfully")
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Export failed")
            }
        }
    }

    fun backupData(context: Context) {
        viewModelScope.launch {
            _exportStatus.value = ExportStatus.Loading
            try {
                val userId = prefs.currentUserId.first()
                val json = repository.exportAllDataAsJson(userId)
                val backupDir = File(context.filesDir, "backups")
                backupDir.mkdirs()
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                val backupFile = File(backupDir, "backup_$timestamp.json")
                backupFile.writeText(json)
                val cleanedCount = backupDir.listFiles()
                    ?.sortedByDescending { it.lastModified() }
                    ?.drop(5)
                    ?.onEach { it.delete() }
                    ?.size ?: 0
                _exportStatus.value = ExportStatus.Success("Backup saved (${backupDir.listFiles()?.size ?: 0} backups stored)")
            } catch (e: Exception) {
                _exportStatus.value = ExportStatus.Error(e.message ?: "Backup failed")
            }
        }
    }
}

sealed class ExportStatus {
    object Idle : ExportStatus()
    object Loading : ExportStatus()
    data class Success(val message: String) : ExportStatus()
    data class Error(val message: String) : ExportStatus()
}
