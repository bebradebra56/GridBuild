package com.gridibuild.sfobud.lgte.presentation.ui.load

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.gridibuild.sfobud.MainActivity
import com.gridibuild.sfobud.R
import com.gridibuild.sfobud.databinding.FragmentLoadGridBuildBinding
import com.gridibuild.sfobud.lgte.data.shar.GridBuildSharedPreference
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel


class GridBuildLoadFragment : Fragment(R.layout.fragment_load_grid_build) {
    private lateinit var gridBuildLoadBinding: FragmentLoadGridBuildBinding

    private val gridBuildLoadViewModel by viewModel<GridBuildLoadViewModel>()

    private val gridBuildSharedPreference by inject<GridBuildSharedPreference>()

    private var gridBuildUrl = ""

    private val gridBuildRequestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        gridBuildSharedPreference.gridBuildNotificationState = 2
        gridBuildNavigateToSuccess(gridBuildUrl)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gridBuildLoadBinding = FragmentLoadGridBuildBinding.bind(view)

        gridBuildLoadBinding.gridBuildGrandButton.setOnClickListener {
            val gridBuildPermission = Manifest.permission.POST_NOTIFICATIONS
            gridBuildRequestNotificationPermission.launch(gridBuildPermission)
        }

        gridBuildLoadBinding.gridBuildSkipButton.setOnClickListener {
            gridBuildSharedPreference.gridBuildNotificationState = 1
            gridBuildSharedPreference.gridBuildNotificationRequest =
                (System.currentTimeMillis() / 1000) + 259200
            gridBuildNavigateToSuccess(gridBuildUrl)
        }

        lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gridBuildLoadViewModel.gridBuildHomeScreenState.collect {
                    when (it) {
                        is GridBuildLoadViewModel.GridBuildHomeScreenState.GridBuildLoading -> {

                        }

                        is GridBuildLoadViewModel.GridBuildHomeScreenState.GridBuildError -> {
                            requireActivity().startActivity(
                                Intent(
                                    requireContext(),
                                    MainActivity::class.java
                                )
                            )
                            requireActivity().finish()
                        }

                        is GridBuildLoadViewModel.GridBuildHomeScreenState.GridBuildSuccess -> {
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2) {
                                val gridBuildNotificationState = gridBuildSharedPreference.gridBuildNotificationState
                                when (gridBuildNotificationState) {
                                    0 -> {
                                        gridBuildLoadBinding.gridBuildNotiGroup.visibility = View.VISIBLE
                                        gridBuildLoadBinding.gridBuildLoadingGroup.visibility = View.GONE
                                        gridBuildUrl = it.data
                                    }
                                    1 -> {
                                        if (System.currentTimeMillis() / 1000 > gridBuildSharedPreference.gridBuildNotificationRequest) {
                                            gridBuildLoadBinding.gridBuildNotiGroup.visibility = View.VISIBLE
                                            gridBuildLoadBinding.gridBuildLoadingGroup.visibility = View.GONE
                                            gridBuildUrl = it.data
                                        } else {
                                            gridBuildNavigateToSuccess(it.data)
                                        }
                                    }
                                    2 -> {
                                        gridBuildNavigateToSuccess(it.data)
                                    }
                                }
                            } else {
                                gridBuildNavigateToSuccess(it.data)
                            }
                        }

                        GridBuildLoadViewModel.GridBuildHomeScreenState.GridBuildNotInternet -> {
                            gridBuildLoadBinding.gridBuildStateGroup.visibility = View.VISIBLE
                            gridBuildLoadBinding.gridBuildLoadingGroup.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }


    private fun gridBuildNavigateToSuccess(data: String) {
        findNavController().navigate(
            R.id.action_gridBuildLoadFragment_to_gridBuildV,
            bundleOf(GRID_BUILD_D to data)
        )
    }

    companion object {
        const val GRID_BUILD_D = "gridBuildData"
    }
}