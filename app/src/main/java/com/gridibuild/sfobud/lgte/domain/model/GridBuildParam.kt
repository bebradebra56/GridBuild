package com.gridibuild.sfobud.lgte.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


private const val GRID_BUILD_A = "com.gridibuild.sfobud"
private const val GRID_BUILD_B = "gridbuild-46d03"
@Serializable
data class GridBuildParam (
    @SerialName("af_id")
    val gridBuildAfId: String,
    @SerialName("bundle_id")
    val gridBuildBundleId: String = GRID_BUILD_A,
    @SerialName("os")
    val gridBuildOs: String = "Android",
    @SerialName("store_id")
    val gridBuildStoreId: String = GRID_BUILD_A,
    @SerialName("locale")
    val gridBuildLocale: String,
    @SerialName("push_token")
    val gridBuildPushToken: String,
    @SerialName("firebase_project_id")
    val gridBuildFirebaseProjectId: String = GRID_BUILD_B,
    )