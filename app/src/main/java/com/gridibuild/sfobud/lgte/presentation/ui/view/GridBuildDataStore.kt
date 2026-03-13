package com.gridibuild.sfobud.lgte.presentation.ui.view

import android.annotation.SuppressLint
import android.widget.FrameLayout
import androidx.lifecycle.ViewModel

class GridBuildDataStore : ViewModel(){
    val gridBuildViList: MutableList<GridBuildVi> = mutableListOf()
    var gridBuildIsFirstCreate = true
    @SuppressLint("StaticFieldLeak")
    lateinit var gridBuildContainerView: FrameLayout
    @SuppressLint("StaticFieldLeak")
    lateinit var gridBuildView: GridBuildVi

}