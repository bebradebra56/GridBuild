package com.gridibuild.sfobud.lgte.presentation.ui.load

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gridibuild.sfobud.lgte.data.shar.GridBuildSharedPreference
import com.gridibuild.sfobud.lgte.data.utils.GridBuildSystemService
import com.gridibuild.sfobud.lgte.domain.usecases.GridBuildGetAllUseCase
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildAppsFlyerState
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GridBuildLoadViewModel(
    private val gridBuildGetAllUseCase: GridBuildGetAllUseCase,
    private val gridBuildSharedPreference: GridBuildSharedPreference,
    private val gridBuildSystemService: GridBuildSystemService
) : ViewModel() {

    private val _gridBuildHomeScreenState: MutableStateFlow<GridBuildHomeScreenState> =
        MutableStateFlow(GridBuildHomeScreenState.GridBuildLoading)
    val gridBuildHomeScreenState = _gridBuildHomeScreenState.asStateFlow()

    private var gridBuildGetApps = false


    init {
        viewModelScope.launch {
            when (gridBuildSharedPreference.gridBuildAppState) {
                0 -> {
                    if (gridBuildSystemService.gridBuildIsOnline()) {
                        GridBuildApplication.gridBuildConversionFlow.collect {
                            when(it) {
                                GridBuildAppsFlyerState.GridBuildDefault -> {}
                                GridBuildAppsFlyerState.GridBuildError -> {
                                    gridBuildSharedPreference.gridBuildAppState = 2
                                    _gridBuildHomeScreenState.value =
                                        GridBuildHomeScreenState.GridBuildError
                                    gridBuildGetApps = true
                                }
                                is GridBuildAppsFlyerState.GridBuildSuccess -> {
                                    if (!gridBuildGetApps) {
                                        gridBuildGetData(it.gridBuildData)
                                        gridBuildGetApps = true
                                    }
                                }
                            }
                        }
                    } else {
                        _gridBuildHomeScreenState.value =
                            GridBuildHomeScreenState.GridBuildNotInternet
                    }
                }
                1 -> {
                    if (gridBuildSystemService.gridBuildIsOnline()) {
                        if (GridBuildApplication.GRID_BUILD_FB_LI != null) {
                            _gridBuildHomeScreenState.value =
                                GridBuildHomeScreenState.GridBuildSuccess(
                                    GridBuildApplication.GRID_BUILD_FB_LI.toString()
                                )
                        } else if (System.currentTimeMillis() / 1000 > gridBuildSharedPreference.gridBuildExpired) {
                            Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Current time more then expired, repeat request")
                            GridBuildApplication.gridBuildConversionFlow.collect {
                                when(it) {
                                    GridBuildAppsFlyerState.GridBuildDefault -> {}
                                    GridBuildAppsFlyerState.GridBuildError -> {
                                        _gridBuildHomeScreenState.value =
                                            GridBuildHomeScreenState.GridBuildSuccess(
                                                gridBuildSharedPreference.gridBuildSavedUrl
                                            )
                                        gridBuildGetApps = true
                                    }
                                    is GridBuildAppsFlyerState.GridBuildSuccess -> {
                                        if (!gridBuildGetApps) {
                                            gridBuildGetData(it.gridBuildData)
                                            gridBuildGetApps = true
                                        }
                                    }
                                }
                            }
                        } else {
                            Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Current time less then expired, use saved url")
                            _gridBuildHomeScreenState.value =
                                GridBuildHomeScreenState.GridBuildSuccess(
                                    gridBuildSharedPreference.gridBuildSavedUrl
                                )
                        }
                    } else {
                        _gridBuildHomeScreenState.value =
                            GridBuildHomeScreenState.GridBuildNotInternet
                    }
                }
                2 -> {
                    _gridBuildHomeScreenState.value =
                        GridBuildHomeScreenState.GridBuildError
                }
            }
        }
    }


    private suspend fun gridBuildGetData(conversation: MutableMap<String, Any>?) {
        val gridBuildData = gridBuildGetAllUseCase.invoke(conversation)
        if (gridBuildSharedPreference.gridBuildAppState == 0) {
            if (gridBuildData == null) {
                gridBuildSharedPreference.gridBuildAppState = 2
                _gridBuildHomeScreenState.value =
                    GridBuildHomeScreenState.GridBuildError
            } else {
                gridBuildSharedPreference.gridBuildAppState = 1
                gridBuildSharedPreference.apply {
                    gridBuildExpired = gridBuildData.gridBuildExpires
                    gridBuildSavedUrl = gridBuildData.gridBuildUrl
                }
                _gridBuildHomeScreenState.value =
                    GridBuildHomeScreenState.GridBuildSuccess(gridBuildData.gridBuildUrl)
            }
        } else  {
            if (gridBuildData == null) {
                _gridBuildHomeScreenState.value =
                    GridBuildHomeScreenState.GridBuildSuccess(gridBuildSharedPreference.gridBuildSavedUrl)
            } else {
                gridBuildSharedPreference.apply {
                    gridBuildExpired = gridBuildData.gridBuildExpires
                    gridBuildSavedUrl = gridBuildData.gridBuildUrl
                }
                _gridBuildHomeScreenState.value =
                    GridBuildHomeScreenState.GridBuildSuccess(gridBuildData.gridBuildUrl)
            }
        }
    }


    sealed class GridBuildHomeScreenState {
        data object GridBuildLoading : GridBuildHomeScreenState()
        data object GridBuildError : GridBuildHomeScreenState()
        data class GridBuildSuccess(val data: String) : GridBuildHomeScreenState()
        data object GridBuildNotInternet: GridBuildHomeScreenState()
    }
}