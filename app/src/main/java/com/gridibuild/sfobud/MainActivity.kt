package com.gridibuild.sfobud

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gridibuild.sfobud.ui.navigation.AppNavigation
import com.gridibuild.sfobud.ui.theme.GridBuildTheme
import com.gridibuild.sfobud.ui.theme.LocalCurrency
import com.gridibuild.sfobud.ui.theme.LocalUnits
import com.gridibuild.sfobud.viewmodel.AuthViewModel
import com.gridibuild.sfobud.viewmodel.SettingsViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val config = Configuration(newBase.resources.configuration)
        config.setLocale(Locale.ENGLISH)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val settingsVm: SettingsViewModel = viewModel()
            val isDark by settingsVm.isDarkTheme.collectAsState()
            val currency by settingsVm.currency.collectAsState()
            val units by settingsVm.units.collectAsState()
            val authVm: AuthViewModel = viewModel()

            GridBuildTheme(darkTheme = isDark) {
                androidx.compose.runtime.CompositionLocalProvider(
                    LocalCurrency provides currency,
                    LocalUnits provides units
                ) {
                    AppNavigation(authViewModel = authVm)
                }
            }
        }
    }
}
