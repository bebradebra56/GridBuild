package com.gridibuild.sfobud.lgte.data.shar

import android.content.Context
import androidx.core.content.edit

class GridBuildSharedPreference(context: Context) {
    private val gridBuildPrefs = context.getSharedPreferences("gridBuildSharedPrefsAb", Context.MODE_PRIVATE)

    var gridBuildSavedUrl: String
        get() = gridBuildPrefs.getString(GRID_BUILD_SAVED_URL, "") ?: ""
        set(value) = gridBuildPrefs.edit { putString(GRID_BUILD_SAVED_URL, value) }

    var gridBuildExpired : Long
        get() = gridBuildPrefs.getLong(GRID_BUILD_EXPIRED, 0L)
        set(value) = gridBuildPrefs.edit { putLong(GRID_BUILD_EXPIRED, value) }

    var gridBuildAppState: Int
        get() = gridBuildPrefs.getInt(GRID_BUILD_APPLICATION_STATE, 0)
        set(value) = gridBuildPrefs.edit { putInt(GRID_BUILD_APPLICATION_STATE, value) }

    var gridBuildNotificationRequest: Long
        get() = gridBuildPrefs.getLong(GRID_BUILD_NOTIFICAITON_REQUEST, 0L)
        set(value) = gridBuildPrefs.edit { putLong(GRID_BUILD_NOTIFICAITON_REQUEST, value) }

    var gridBuildNotificationState:Int
        get() = gridBuildPrefs.getInt(GRID_BUILD_NOTIFICATION_STATE, 0)
        set(value) = gridBuildPrefs.edit { putInt(GRID_BUILD_NOTIFICATION_STATE, value) }

    companion object {
        private const val GRID_BUILD_NOTIFICATION_STATE = "gridBuildNotificationState"
        private const val GRID_BUILD_SAVED_URL = "gridBuildSavedUrl"
        private const val GRID_BUILD_EXPIRED = "gridBuildExpired"
        private const val GRID_BUILD_APPLICATION_STATE = "gridBuildApplicationState"
        private const val GRID_BUILD_NOTIFICAITON_REQUEST = "gridBuildNotificationRequest"
    }
}