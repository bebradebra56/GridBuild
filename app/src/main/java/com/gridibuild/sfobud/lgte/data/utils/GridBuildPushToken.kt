package com.gridibuild.sfobud.lgte.data.utils

import android.util.Log
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import java.lang.Exception

class GridBuildPushToken {

    suspend fun gridBuildGetToken(
        gridBuildMaxAttempts: Int = 3,
        gridBuildDelayMs: Long = 1500
    ): String {

        repeat(gridBuildMaxAttempts - 1) {
            try {
                val gridBuildToken = FirebaseMessaging.getInstance().token.await()
                return gridBuildToken
            } catch (e: Exception) {
                Log.e(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Token error (attempt ${it + 1}): ${e.message}")
                delay(gridBuildDelayMs)
            }
        }

        return try {
            FirebaseMessaging.getInstance().token.await()
        } catch (e: Exception) {
            Log.e(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Token error final: ${e.message}")
            "null"
        }
    }


}