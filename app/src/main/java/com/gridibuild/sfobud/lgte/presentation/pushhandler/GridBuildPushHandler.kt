package com.gridibuild.sfobud.lgte.presentation.pushhandler

import android.os.Bundle
import android.util.Log
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication

class GridBuildPushHandler {
    fun gridBuildHandlePush(extras: Bundle?) {
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Extras from Push = ${extras?.keySet()}")
        if (extras != null) {
            val map: MutableMap<String, String?> = HashMap()
            val ks = extras.keySet()
            val iterator: Iterator<String> = ks.iterator()
            while (iterator.hasNext()) {
                val key = iterator.next()
                map[key] = extras.getString(key)
            }
            Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Map from Push = $map")
            map.let {
                if (map.containsKey("url")) {
                    GridBuildApplication.GRID_BUILD_FB_LI = map["url"]
                    Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "UrlFromActivity = $map")
                }
            }
        } else {
            Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Push data no!")
        }
    }

}