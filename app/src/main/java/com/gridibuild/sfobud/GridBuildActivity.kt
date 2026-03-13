package com.gridibuild.sfobud

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.gridibuild.sfobud.lgte.GridBuildGlobalLayoutUtil
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication
import com.gridibuild.sfobud.lgte.presentation.pushhandler.GridBuildPushHandler
import com.gridibuild.sfobud.lgte.gridBuildSetupSystemBars
import org.koin.android.ext.android.inject

class GridBuildActivity : AppCompatActivity() {

    private val gridBuildPushHandler by inject<GridBuildPushHandler>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gridBuildSetupSystemBars()
        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContentView(R.layout.activity_grid_build)

        val gridBuildRootView = findViewById<View>(android.R.id.content)
        GridBuildGlobalLayoutUtil().gridBuildAssistActivity(this)
        ViewCompat.setOnApplyWindowInsetsListener(gridBuildRootView) { gridBuildView, gridBuildInsets ->
            val gridBuildSystemBars = gridBuildInsets.getInsets(WindowInsetsCompat.Type.systemBars())
            val gridBuildDisplayCutout = gridBuildInsets.getInsets(WindowInsetsCompat.Type.displayCutout())
            val gridBuildIme = gridBuildInsets.getInsets(WindowInsetsCompat.Type.ime())


            val gridBuildTopPadding = maxOf(gridBuildSystemBars.top, gridBuildDisplayCutout.top)
            val gridBuildLeftPadding = maxOf(gridBuildSystemBars.left, gridBuildDisplayCutout.left)
            val gridBuildRightPadding = maxOf(gridBuildSystemBars.right, gridBuildDisplayCutout.right)
            window.setSoftInputMode(GridBuildApplication.gridBuildInputMode)

            if (window.attributes.softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN) {
                Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "ADJUST PUN")
                val gridBuildBottomInset = maxOf(gridBuildSystemBars.bottom, gridBuildDisplayCutout.bottom)

                gridBuildView.setPadding(gridBuildLeftPadding, gridBuildTopPadding, gridBuildRightPadding, 0)

                gridBuildView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = gridBuildBottomInset
                }
            } else {
                Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "ADJUST RESIZE")

                val gridBuildBottomInset = maxOf(gridBuildSystemBars.bottom, gridBuildDisplayCutout.bottom, gridBuildIme.bottom)

                gridBuildView.setPadding(gridBuildLeftPadding, gridBuildTopPadding, gridBuildRightPadding, 0)

                gridBuildView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                    bottomMargin = gridBuildBottomInset
                }
            }



            WindowInsetsCompat.CONSUMED
        }
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Activity onCreate()")
        gridBuildPushHandler.gridBuildHandlePush(intent.extras)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            gridBuildSetupSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        gridBuildSetupSystemBars()
    }
}