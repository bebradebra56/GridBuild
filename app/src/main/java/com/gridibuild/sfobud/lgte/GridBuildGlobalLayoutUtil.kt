package com.gridibuild.sfobud.lgte

import android.app.Activity
import android.graphics.Rect
import android.view.View
import android.widget.FrameLayout
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication

class GridBuildGlobalLayoutUtil {

    private var gridBuildMChildOfContent: View? = null
    private var gridBuildUsableHeightPrevious = 0

    fun gridBuildAssistActivity(activity: Activity) {
        val content = activity.findViewById<FrameLayout>(android.R.id.content)
        gridBuildMChildOfContent = content.getChildAt(0)

        gridBuildMChildOfContent?.viewTreeObserver?.addOnGlobalLayoutListener {
            possiblyResizeChildOfContent(activity)
        }
    }

    private fun possiblyResizeChildOfContent(activity: Activity) {
        val gridBuildUsableHeightNow = gridBuildComputeUsableHeight()
        if (gridBuildUsableHeightNow != gridBuildUsableHeightPrevious) {
            val gridBuildUsableHeightSansKeyboard = gridBuildMChildOfContent?.rootView?.height ?: 0
            val gridBuildHeightDifference = gridBuildUsableHeightSansKeyboard - gridBuildUsableHeightNow

            if (gridBuildHeightDifference > (gridBuildUsableHeightSansKeyboard / 4)) {
                activity.window.setSoftInputMode(GridBuildApplication.gridBuildInputMode)
            } else {
                activity.window.setSoftInputMode(GridBuildApplication.gridBuildInputMode)
            }
//            mChildOfContent?.requestLayout()
            gridBuildUsableHeightPrevious = gridBuildUsableHeightNow
        }
    }

    private fun gridBuildComputeUsableHeight(): Int {
        val r = Rect()
        gridBuildMChildOfContent?.getWindowVisibleDisplayFrame(r)
        return r.bottom - r.top  // Visible height без status bar
    }
}