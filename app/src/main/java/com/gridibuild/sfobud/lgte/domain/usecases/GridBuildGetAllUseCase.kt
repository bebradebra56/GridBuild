package com.gridibuild.sfobud.lgte.domain.usecases

import android.util.Log
import com.gridibuild.sfobud.lgte.data.repo.GridBuildRepository
import com.gridibuild.sfobud.lgte.data.utils.GridBuildPushToken
import com.gridibuild.sfobud.lgte.data.utils.GridBuildSystemService
import com.gridibuild.sfobud.lgte.domain.model.GridBuildEntity
import com.gridibuild.sfobud.lgte.domain.model.GridBuildParam
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication

class GridBuildGetAllUseCase(
    private val gridBuildRepository: GridBuildRepository,
    private val gridBuildSystemService: GridBuildSystemService,
    private val gridBuildPushToken: GridBuildPushToken,
) {
    suspend operator fun invoke(conversion: MutableMap<String, Any>?) : GridBuildEntity?{
        val params = GridBuildParam(
            gridBuildLocale = gridBuildSystemService.gridBuildGetLocale(),
            gridBuildPushToken = gridBuildPushToken.gridBuildGetToken(),
            gridBuildAfId = gridBuildSystemService.gridBuildGetAppsflyerId()
        )
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Params for request: $params")
        return gridBuildRepository.gridBuildGetClient(params, conversion)
    }



}