package com.gridibuild.sfobud.lgte.presentation.di

import com.gridibuild.sfobud.lgte.data.repo.GridBuildRepository
import com.gridibuild.sfobud.lgte.data.shar.GridBuildSharedPreference
import com.gridibuild.sfobud.lgte.data.utils.GridBuildPushToken
import com.gridibuild.sfobud.lgte.data.utils.GridBuildSystemService
import com.gridibuild.sfobud.lgte.domain.usecases.GridBuildGetAllUseCase
import com.gridibuild.sfobud.lgte.presentation.pushhandler.GridBuildPushHandler
import com.gridibuild.sfobud.lgte.presentation.ui.load.GridBuildLoadViewModel
import com.gridibuild.sfobud.lgte.presentation.ui.view.GridBuildViFun
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val gridBuildModule = module {
    factory {
        GridBuildPushHandler()
    }
    single {
        GridBuildRepository()
    }
    single {
        GridBuildSharedPreference(get())
    }
    factory {
        GridBuildPushToken()
    }
    factory {
        GridBuildSystemService(get())
    }
    factory {
        GridBuildGetAllUseCase(
            get(), get(), get()
        )
    }
    factory {
        GridBuildViFun(get())
    }
    viewModel {
        GridBuildLoadViewModel(get(), get(), get())
    }
}