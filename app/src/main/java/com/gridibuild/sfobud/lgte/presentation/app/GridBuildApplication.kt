package com.gridibuild.sfobud.lgte.presentation.app

import android.app.Application
import android.util.Log
import android.view.WindowManager
import com.appsflyer.AppsFlyerConversionListener
import com.appsflyer.AppsFlyerLib
import com.appsflyer.attribution.AppsFlyerRequestListener
import com.appsflyer.deeplink.DeepLink
import com.appsflyer.deeplink.DeepLinkListener
import com.appsflyer.deeplink.DeepLinkResult
import com.gridibuild.sfobud.lgte.presentation.di.gridBuildModule
import com.gridibuild.sfobud.data.local.AppDatabase
import com.gridibuild.sfobud.data.repository.AppRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level


sealed interface GridBuildAppsFlyerState {
    data object GridBuildDefault : GridBuildAppsFlyerState
    data class GridBuildSuccess(val gridBuildData: MutableMap<String, Any>?) :
        GridBuildAppsFlyerState

    data object GridBuildError : GridBuildAppsFlyerState
}


private const val GRID_BUILD_APP_DEV = "DHstSAE2ALqoQdhzEUDcGM"
private const val GRID_BUILD_LIN = "com.gridibuild.sfobud"

class GridBuildApplication : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val repository by lazy { AppRepository(database) }

    private val gridBuildKtorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
        }

    }


    private var gridBuildIsResumed = false
//    private var gridBuildConversionTimeoutJob: Job? = null
    private var gridBuildDeepLinkData: MutableMap<String, Any>? = null

    override fun onCreate() {
        super.onCreate()

        val appsflyer = AppsFlyerLib.getInstance()
        gridBuildSetDebufLogger(appsflyer)
        gridBuildMinTimeBetween(appsflyer)

        AppsFlyerLib.getInstance().subscribeForDeepLink(object : DeepLinkListener {
            override fun onDeepLinking(p0: DeepLinkResult) {
                when (p0.status) {
                    DeepLinkResult.Status.FOUND -> {
                        gridBuildExtractDeepMap(p0.deepLink)
                        Log.d(GRID_BUILD_MAIN_TAG, "onDeepLinking found: ${p0.deepLink}")

                    }

                    DeepLinkResult.Status.NOT_FOUND -> {
                        Log.d(GRID_BUILD_MAIN_TAG, "onDeepLinking not found: ${p0.deepLink}")
                    }

                    DeepLinkResult.Status.ERROR -> {
                        Log.d(GRID_BUILD_MAIN_TAG, "onDeepLinking error: ${p0.error}")
                    }
                }
            }

        })


        appsflyer.init(
            GRID_BUILD_APP_DEV,
            object : AppsFlyerConversionListener {
                override fun onConversionDataSuccess(p0: MutableMap<String, Any>?) {
//                    gridBuildConversionTimeoutJob?.cancel()
                    Log.d(GRID_BUILD_MAIN_TAG, "onConversionDataSuccess: $p0")

                    val afStatus = p0?.get("af_status")?.toString() ?: "null"
                    if (afStatus == "Organic") {
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                delay(5000)
                                val response = gridBuildKtorClient.get("https://gcdsdk.appsflyer.com/install_data/v4.0/$GRID_BUILD_LIN") {
                                    parameter("devkey", GRID_BUILD_APP_DEV)
                                    parameter("device_id", gridBuildGetAppsflyerId())
                                }

                                val resp = response.body<MutableMap<String, JsonElement>?>()
                                val f = resp?.mapValues { (_, v) -> jsonElementToAny(v) }?.toMutableMap() ?: mutableMapOf()
                                Log.d(GRID_BUILD_MAIN_TAG, "After 5s: $resp")
                                if (resp?.get("af_status")?.jsonPrimitive?.content == "Organic" || resp?.get("af_status") == null) {
                                    gridBuildResume(
                                        GridBuildAppsFlyerState.GridBuildError
                                    )
                                } else {
                                    gridBuildResume(
                                        GridBuildAppsFlyerState.GridBuildSuccess(
                                            f
                                        )
                                    )
                                }
                            } catch (d: Exception) {
                                Log.d(GRID_BUILD_MAIN_TAG, "Error: ${d.message}")
                                gridBuildResume(GridBuildAppsFlyerState.GridBuildError)
                            }
                        }
                    } else {
                        gridBuildResume(GridBuildAppsFlyerState.GridBuildSuccess(p0))
                    }
                }

                override fun onConversionDataFail(p0: String?) {
//                    gridBuildConversionTimeoutJob?.cancel()
                    Log.d(GRID_BUILD_MAIN_TAG, "onConversionDataFail: $p0")
                    gridBuildResume(GridBuildAppsFlyerState.GridBuildError)
                }

                override fun onAppOpenAttribution(p0: MutableMap<String, String>?) {
                    Log.d(GRID_BUILD_MAIN_TAG, "onAppOpenAttribution")
                }

                override fun onAttributionFailure(p0: String?) {
                    Log.d(GRID_BUILD_MAIN_TAG, "onAttributionFailure: $p0")
                }
            },
            this
        )

        appsflyer.start(this, GRID_BUILD_APP_DEV, object :
            AppsFlyerRequestListener {
            override fun onSuccess() {
                Log.d(GRID_BUILD_MAIN_TAG, "AppsFlyer started")
            }

            override fun onError(p0: Int, p1: String) {
                Log.d(GRID_BUILD_MAIN_TAG, "AppsFlyer start error: $p0 - $p1")
            }
        })
//        gridBuildStartConversionTimeout()
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@GridBuildApplication)
            modules(
                listOf(
                    gridBuildModule
                )
            )
        }
    }

    fun jsonElementToAny(element: JsonElement): Any {
        return when (element) {
            is JsonPrimitive -> {
                when {
                    element.isString -> element.content
                    element.booleanOrNull != null -> element.boolean
                    element.longOrNull != null -> element.long
                    element.doubleOrNull != null -> element.double
                    else -> element.content
                }
            }
            is JsonObject -> element.mapValues { (_, v) -> jsonElementToAny(v) }
            is JsonArray -> element.map { jsonElementToAny(it) }

        }
    }

    private fun gridBuildExtractDeepMap(dl: DeepLink) {
        val map = mutableMapOf<String, Any>()
        dl.deepLinkValue?.let { map["deep_link_value"] = it }
        dl.mediaSource?.let { map["media_source"] = it }
        dl.campaign?.let { map["campaign"] = it }
        dl.campaignId?.let { map["campaign_id"] = it }
        dl.afSub1?.let { map["af_sub1"] = it }
        dl.afSub2?.let { map["af_sub2"] = it }
        dl.afSub3?.let { map["af_sub3"] = it }
        dl.afSub4?.let { map["af_sub4"] = it }
        dl.afSub5?.let { map["af_sub5"] = it }
        dl.matchType?.let { map["match_type"] = it }
        dl.clickHttpReferrer?.let { map["click_http_referrer"] = it }
        dl.getStringValue("timestamp")?.let { map["timestamp"] = it }
        dl.isDeferred?.let { map["is_deferred"] = it }
        for (i in 1..10) {
            val key = "deep_link_sub$i"
            dl.getStringValue(key)?.let {
                if (!map.containsKey(key)) {
                    map[key] = it
                }
            }
        }
        Log.d(GRID_BUILD_MAIN_TAG, "Extracted DeepLink data: $map")
        gridBuildDeepLinkData = map
    }

//    private fun gridBuildStartConversionTimeout() {
//        gridBuildConversionTimeoutJob = CoroutineScope(Dispatchers.Main).launch {
//            delay(30000)
//            if (!gridBuildIsResumed) {
//                Log.d(PLINK_ZEN_MAIN_TAG, "TIMEOUT: No conversion data received in 30s")
//                gridBuildResume(PlinkZenAppsFlyerState.PlinkZenError)
//            }
//        }
//    }

    private fun gridBuildResume(state: GridBuildAppsFlyerState) {
//        gridBuildConversionTimeoutJob?.cancel()
        if (state is GridBuildAppsFlyerState.GridBuildSuccess) {
            val convData = state.gridBuildData ?: mutableMapOf()
            val deepData = gridBuildDeepLinkData ?: mutableMapOf()
            val merged = mutableMapOf<String, Any>().apply {
                putAll(convData)
                for ((key, value) in deepData) {
                    if (!containsKey(key)) {
                        put(key, value)
                    }
                }
            }
            if (!gridBuildIsResumed) {
                gridBuildIsResumed = true
                gridBuildConversionFlow.value =
                    GridBuildAppsFlyerState.GridBuildSuccess(merged)
            }
        } else {
            if (!gridBuildIsResumed) {
                gridBuildIsResumed = true
                gridBuildConversionFlow.value = state
            }
        }
    }

    private fun gridBuildGetAppsflyerId(): String {
        val appsflyrid = AppsFlyerLib.getInstance().getAppsFlyerUID(this) ?: ""
        Log.d(GRID_BUILD_MAIN_TAG, "AppsFlyer: AppsFlyer Id = $appsflyrid")
        return appsflyrid
    }

    private fun gridBuildSetDebufLogger(appsflyer: AppsFlyerLib) {
        appsflyer.setDebugLog(true)
    }

    private fun gridBuildMinTimeBetween(appsflyer: AppsFlyerLib) {
        appsflyer.setMinTimeBetweenSessions(0)
    }

    companion object {

        var gridBuildInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN
        val gridBuildConversionFlow: MutableStateFlow<GridBuildAppsFlyerState> = MutableStateFlow(
            GridBuildAppsFlyerState.GridBuildDefault
        )
        var GRID_BUILD_FB_LI: String? = null
        const val GRID_BUILD_MAIN_TAG = "GridBuildMainTag"
    }
}