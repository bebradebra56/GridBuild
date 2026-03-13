package com.gridibuild.sfobud.lgte.data.repo

import android.util.Log
import com.gridibuild.sfobud.lgte.domain.model.GridBuildEntity
import com.gridibuild.sfobud.lgte.domain.model.GridBuildParam
import com.gridibuild.sfobud.lgte.presentation.app.GridBuildApplication.Companion.GRID_BUILD_MAIN_TAG
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.plugin
import io.ktor.client.request.accept
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer



private const val GRID_BUILD_MAIN = "https://grridbuilld.com/config.php"

class GridBuildRepository {


    private val gridBuildKtorClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            })
        }
        install(HttpTimeout) {
            connectTimeoutMillis = 30000
            socketTimeoutMillis = 30000
            requestTimeoutMillis = 30000
        }

    }

    suspend fun gridBuildGetClient(
        gridBuildParam: GridBuildParam,
        gridBuildConversion: MutableMap<String, Any>?
    ): GridBuildEntity? =
        withContext(Dispatchers.IO) {
            gridBuildKtorClient.plugin(HttpSend).intercept { request ->
                Log.d(GRID_BUILD_MAIN_TAG, "Ktor: Intercept body ${request.body}")
                execute(request)
            }
            val gridBuildJson = Json {
                ignoreUnknownKeys = true
                encodeDefaults = true
            }
            Log.d(
                GRID_BUILD_MAIN_TAG,
                "Ktor: conversation json: ${gridBuildConversion.toString()}"
            )
            val gridBuildBody = gridBuildMergeToFlatJson(
                json = gridBuildJson,
                param = gridBuildParam,
                conversation = gridBuildConversion
            )
            Log.d(
                GRID_BUILD_MAIN_TAG,
                "Ktor: request json: $gridBuildBody"
            )
            return@withContext try {
                val response = gridBuildKtorClient.post(GRID_BUILD_MAIN) {
                    contentType(ContentType.Application.Json) // обязательно JSON
                    accept(ContentType.Application.Json)
                    setBody(gridBuildBody) // JsonObject
                }
                val code = response.status.value
                Log.d(GRID_BUILD_MAIN_TAG, "Ktor: Request status code: $code")
                if (code == 200) {
                    val rawBody = response.bodyAsText() // читаем ответ как текст
                    val gridBuildEntity = Json { ignoreUnknownKeys = true }
                        .decodeFromString(GridBuildEntity.serializer(), rawBody)
                    Log.d(GRID_BUILD_MAIN_TAG, "Ktor: Get request success")
                    Log.d(GRID_BUILD_MAIN_TAG, "Ktor: $gridBuildEntity")
                    gridBuildEntity
                } else {
                    Log.d(GRID_BUILD_MAIN_TAG, "Ktor: Status code invalid, return null")
                    Log.d(GRID_BUILD_MAIN_TAG, "Ktor: ${response.body<String>()}")
                    null
                }

            } catch (e: Exception) {
                Log.d(GRID_BUILD_MAIN_TAG, "Ktor: Get request failed")
                Log.d(GRID_BUILD_MAIN_TAG, "Ktor: ${e.message}")
                null
            }
        }

    private inline fun <reified T> Json.gridBuildEncodeToJsonObject(value: T): JsonObject =
        encodeToJsonElement(serializer(), value).jsonObject

    private inline fun <reified T> gridBuildMergeToFlatJson(
        json: Json,
        param: T,
        conversation: Map<String, Any>?
    ): JsonObject {

        val paramJson = json.gridBuildEncodeToJsonObject(param)

        return buildJsonObject {
            // поля из param
            paramJson.forEach { (key, value) ->
                put(key, value)
            }

            // динамические поля
            conversation?.forEach { (key, value) ->
                put(key, gridBuildAnyToJsonElement(value))
            }
        }
    }

    private fun gridBuildAnyToJsonElement(value: Any?): JsonElement {
        return when (value) {
            null -> JsonNull
            is String -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is Boolean -> JsonPrimitive(value)
            is Map<*, *> -> buildJsonObject {
                value.forEach { (k, v) ->
                    if (k is String) {
                        put(k, gridBuildAnyToJsonElement(v))
                    }
                }
            }
            is List<*> -> buildJsonArray {
                value.forEach {
                    add(gridBuildAnyToJsonElement(it))
                }
            }
            else -> JsonPrimitive(value.toString())
        }
    }


}
