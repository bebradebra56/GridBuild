package com.gridibuild.sfobud.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferences(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val CURRENT_USER_ID = longPreferencesKey("current_user_id")
        val IS_ONBOARDING_DONE = booleanPreferencesKey("is_onboarding_done")
        val CURRENT_PROJECT_ID = longPreferencesKey("current_project_id")
        val CURRENCY = stringPreferencesKey("currency")
        val UNITS = stringPreferencesKey("units")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val currentUserId: Flow<Long> = dataStore.data.map { it[CURRENT_USER_ID] ?: -1L }
    val isOnboardingDone: Flow<Boolean> = dataStore.data.map { it[IS_ONBOARDING_DONE] ?: false }
    val currentProjectId: Flow<Long> = dataStore.data.map { it[CURRENT_PROJECT_ID] ?: -1L }
    val currency: Flow<String> = dataStore.data.map { it[CURRENCY] ?: "USD" }
    val units: Flow<String> = dataStore.data.map { it[UNITS] ?: "m" }
    val isDarkTheme: Flow<Boolean> = dataStore.data.map { it[IS_DARK_THEME] ?: false }
    val notificationsEnabled: Flow<Boolean> = dataStore.data.map { it[NOTIFICATIONS_ENABLED] ?: true }

    suspend fun setCurrentUserId(userId: Long) = dataStore.edit { it[CURRENT_USER_ID] = userId }
    suspend fun setOnboardingDone(done: Boolean) = dataStore.edit { it[IS_ONBOARDING_DONE] = done }
    suspend fun setCurrentProjectId(id: Long) = dataStore.edit { it[CURRENT_PROJECT_ID] = id }
    suspend fun setCurrency(currency: String) = dataStore.edit { it[CURRENCY] = currency }
    suspend fun setUnits(units: String) = dataStore.edit { it[UNITS] = units }
    suspend fun setDarkTheme(isDark: Boolean) = dataStore.edit { it[IS_DARK_THEME] = isDark }
    suspend fun setNotificationsEnabled(enabled: Boolean) = dataStore.edit { it[NOTIFICATIONS_ENABLED] = enabled }
    suspend fun clearAll() = dataStore.edit { it.clear() }
}
