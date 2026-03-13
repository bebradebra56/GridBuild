package com.gridibuild.sfobud.lgte.presentation.ui.view

import android.content.DialogInterface
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.widget.FrameLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication
import com.gridibuild.sfobud.lgte.presentation.ui.load.GridBuildLoadFragment
import org.koin.android.ext.android.inject

class GridBuildV : Fragment(){

    private lateinit var gridBuildPhoto: Uri
    private var gridBuildFilePathFromChrome: ValueCallback<Array<Uri>>? = null

    private val gridBuildTakeFile: ActivityResultLauncher<PickVisualMediaRequest> = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        gridBuildFilePathFromChrome?.onReceiveValue(arrayOf(it ?: Uri.EMPTY))
        gridBuildFilePathFromChrome = null
    }

    private val gridBuildTakePhoto: ActivityResultLauncher<Uri> = registerForActivityResult(ActivityResultContracts.TakePicture()) {
        if (it) {
            gridBuildFilePathFromChrome?.onReceiveValue(arrayOf(gridBuildPhoto))
            gridBuildFilePathFromChrome = null
        } else {
            gridBuildFilePathFromChrome?.onReceiveValue(null)
            gridBuildFilePathFromChrome = null
        }
    }

    private val gridBuildDataStore by activityViewModels<GridBuildDataStore>()


    private val gridBuildViFun by inject<GridBuildViFun>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Fragment onCreate")
        CookieManager.getInstance().setAcceptCookie(true)
        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (gridBuildDataStore.gridBuildView.canGoBack()) {
                        gridBuildDataStore.gridBuildView.goBack()
                        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "WebView can go back")
                    } else if (gridBuildDataStore.gridBuildViList.size > 1) {
                        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "WebView can`t go back")
                        gridBuildDataStore.gridBuildViList.removeAt(gridBuildDataStore.gridBuildViList.lastIndex)
                        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "WebView list size ${gridBuildDataStore.gridBuildViList.size}")
                        gridBuildDataStore.gridBuildView.destroy()
                        val previousWebView = gridBuildDataStore.gridBuildViList.last()
                        gridBuildAttachWebViewToContainer(previousWebView)
                        gridBuildDataStore.gridBuildView = previousWebView
                    }
                }

            })
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (gridBuildDataStore.gridBuildIsFirstCreate) {
            gridBuildDataStore.gridBuildIsFirstCreate = false
            gridBuildDataStore.gridBuildContainerView = FrameLayout(requireContext()).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                id = View.generateViewId()
            }
            return gridBuildDataStore.gridBuildContainerView
        } else {
            return gridBuildDataStore.gridBuildContainerView
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "onViewCreated")
        if (gridBuildDataStore.gridBuildViList.isEmpty()) {
            gridBuildDataStore.gridBuildView = GridBuildVi(requireContext(), object :
                GridBuildCallBack {
                override fun gridBuildHandleCreateWebWindowRequest(gridBuildVi: GridBuildVi) {
                    gridBuildDataStore.gridBuildViList.add(gridBuildVi)
                    Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "WebView list size = ${gridBuildDataStore.gridBuildViList.size}")
                    Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "CreateWebWindowRequest")
                    gridBuildDataStore.gridBuildView = gridBuildVi
                    gridBuildVi.gridBuildSetFileChooserHandler { callback ->
                        gridBuildHandleFileChooser(callback)
                    }
                    gridBuildAttachWebViewToContainer(gridBuildVi)
                }

            }, gridBuildWindow = requireActivity().window).apply {
                gridBuildSetFileChooserHandler { callback ->
                    gridBuildHandleFileChooser(callback)
                }
            }
            gridBuildDataStore.gridBuildView.gridBuildFLoad(arguments?.getString(
                GridBuildLoadFragment.GRID_BUILD_D) ?: "")
//            ejvview.fLoad("www.google.com")
            gridBuildDataStore.gridBuildViList.add(gridBuildDataStore.gridBuildView)
            gridBuildAttachWebViewToContainer(gridBuildDataStore.gridBuildView)
        } else {
            gridBuildDataStore.gridBuildViList.forEach { webView ->
                webView.gridBuildSetFileChooserHandler { callback ->
                    gridBuildHandleFileChooser(callback)
                }
            }
            gridBuildDataStore.gridBuildView = gridBuildDataStore.gridBuildViList.last()

            gridBuildAttachWebViewToContainer(gridBuildDataStore.gridBuildView)
        }
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "WebView list size = ${gridBuildDataStore.gridBuildViList.size}")
    }

    private fun gridBuildHandleFileChooser(callback: ValueCallback<Array<Uri>>?) {
        Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "handleFileChooser called, callback: ${callback != null}")

        gridBuildFilePathFromChrome = callback

        val listItems: Array<out String> = arrayOf("Select from file", "To make a photo")
        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> {
                    Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Launching file picker")
                    gridBuildTakeFile.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }
                1 -> {
                    Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "Launching camera")
                    gridBuildPhoto = gridBuildViFun.gridBuildSavePhoto()
                    gridBuildTakePhoto.launch(gridBuildPhoto)
                }
            }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Choose a method")
            .setItems(listItems, listener)
            .setCancelable(true)
            .setOnCancelListener {
                Log.d(GridBuildApplication.GRID_BUILD_MAIN_TAG, "File chooser canceled")
                callback?.onReceiveValue(null)
                gridBuildFilePathFromChrome = null
            }
            .create()
            .show()
    }

    private fun gridBuildAttachWebViewToContainer(w: GridBuildVi) {
        gridBuildDataStore.gridBuildContainerView.post {
            (w.parent as? ViewGroup)?.removeView(w)
            gridBuildDataStore.gridBuildContainerView.removeAllViews()
            gridBuildDataStore.gridBuildContainerView.addView(w)
        }
    }


}