package com.gridibuild.sfobud.lgte.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class GridBuildEntity (
    @SerialName("ok")
    val gridBuildOk: Boolean,
    @SerialName("url")
    val gridBuildUrl: String,
    @SerialName("expires")
    val gridBuildExpires: Long,
)