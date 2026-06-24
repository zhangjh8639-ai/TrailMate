package com.trailmate.app.core.network

import org.json.JSONArray
import org.json.JSONObject

internal object TrailMateJson {
    fun quote(value: String): String =
        JSONObject.quote(value)

    fun objectOrEmpty(raw: String): JSONObject =
        runCatching { JSONObject(raw) }.getOrDefault(JSONObject())

    fun arrayOrEmpty(raw: String): JSONArray =
        runCatching { JSONArray(raw) }.getOrDefault(JSONArray())

    fun apiError(raw: String, statusCode: Int): TrailMateApiError {
        val json = objectOrEmpty(raw)
        return TrailMateApiError(
            status = json.nullableInt("status") ?: statusCode,
            code = json.nullableString("code") ?: "HTTP_$statusCode",
            message = json.nullableString("message") ?: "Request failed.",
            traceId = json.nullableString("traceId")
        )
    }
}

internal fun JSONObject.nullableString(key: String): String? =
    if (has(key) && !isNull(key)) {
        runCatching { getString(key) }.getOrNull()
    } else {
        null
    }

internal fun JSONObject.nullableInt(key: String): Int? =
    if (has(key) && !isNull(key)) {
        runCatching { getInt(key) }.getOrNull()
    } else {
        null
    }

internal fun JSONObject.nullableLong(key: String): Long? =
    if (has(key) && !isNull(key)) {
        runCatching { getLong(key) }.getOrNull()
    } else {
        null
    }

internal fun JSONObject.nullableDouble(key: String): Double? =
    if (has(key) && !isNull(key)) {
        runCatching { getDouble(key) }.getOrNull()
    } else {
        null
    }

internal fun JSONObject.stringList(key: String): List<String> =
    optJSONArray(key)?.stringList().orEmpty()

internal fun JSONArray.stringList(): List<String> =
    (0 until length()).mapNotNull { index ->
        if (isNull(index)) {
            null
        } else {
            runCatching { getString(index) }.getOrNull()
        }
    }

internal fun JSONArray.objectList(): List<JSONObject> =
    (0 until length()).mapNotNull { index -> optJSONObject(index) }

internal inline fun <reified T : Enum<T>> JSONObject.enumValue(key: String, fallback: T): T =
    nullableString(key)?.let { rawValue ->
        enumValues<T>().firstOrNull { item -> item.name == rawValue }
    } ?: fallback
