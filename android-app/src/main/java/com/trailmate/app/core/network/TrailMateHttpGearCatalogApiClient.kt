package com.trailmate.app.core.network

import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class TrailMateHttpGearCatalogApiClient(
    private val baseUrl: String,
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000
) : TrailMateGearCatalogApi {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')

    override fun listGearCatalogCategories(): TrailMateApiResult<List<String>> =
        requestJson(
            method = "GET",
            path = TrailMateServerApiContract.Endpoints.gearCatalogCategories,
            parseSuccess = { json -> TrailMateJson.arrayOrEmpty(json).stringList() }
        )

    override fun searchGearCatalog(
        category: String,
        query: String
    ): TrailMateApiResult<List<TrailMateGearCatalogItemDto>> =
        requestJson(
            method = "GET",
            path = TrailMateServerApiContract.Endpoints.gearCatalogSearch,
            query = listOf(
                "category" to category,
                "q" to query
            ),
            parseSuccess = { json ->
                TrailMateJson.arrayOrEmpty(json).objectList().map { objectJson -> objectJson.toCatalogItem() }
            }
        )

    private fun <T> requestJson(
        method: String,
        path: String,
        query: List<Pair<String, String>> = emptyList(),
        body: String? = null,
        parseSuccess: (String) -> T
    ): TrailMateApiResult<T> {
        val url = URL(normalizedBaseUrl + TrailMateServerApiContract.BASE_PATH + path + query.suffix())
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = method
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.setRequestProperty("Accept", "application/json")
            if (body != null) {
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
                OutputStreamWriter(connection.outputStream, Charsets.UTF_8).use { writer ->
                    writer.write(body.compactJson())
                }
            }

            val responseCode = connection.responseCode
            val responseBody = if (responseCode in 200..299) {
                connection.inputStream.bufferedReader(Charsets.UTF_8).readText()
            } else {
                connection.errorStream?.bufferedReader(Charsets.UTF_8)?.readText().orEmpty()
            }
            if (responseCode in 200..299) {
                TrailMateApiResult.Success(parseSuccess(responseBody))
            } else {
                TrailMateApiResult.Failure(responseBody.toApiError(responseCode))
            }
        } catch (exception: Exception) {
            TrailMateApiResult.Failure(
                TrailMateApiError(
                    status = 0,
                    code = "NETWORK_ERROR",
                    message = exception.message ?: "Network request failed.",
                    traceId = null
                )
            )
        } finally {
            connection.disconnect()
        }
    }

    private fun JSONObject.toCatalogItem(): TrailMateGearCatalogItemDto =
        TrailMateGearCatalogItemDto(
            catalogItemId = nullableString("catalogItemId").orEmpty(),
            category = nullableString("category").orEmpty(),
            brand = nullableString("brand").orEmpty(),
            model = nullableString("model").orEmpty(),
            displayName = nullableString("displayName").orEmpty(),
            weightGrams = nullableInt("weightGrams"),
            tags = stringList("tags"),
            imageUrl = nullableString("imageUrl").resolveServerAssetUrl(),
            imageAttribution = nullableString("imageAttribution"),
            source = nullableString("source").orEmpty()
        )

    private fun String.toApiError(statusCode: Int): TrailMateApiError =
        TrailMateJson.apiError(this, statusCode)

    private fun List<Pair<String, String>>.suffix(): String =
        if (isEmpty()) {
            ""
        } else {
            joinToString(prefix = "?", separator = "&") { (key, value) ->
                "${key.urlEncoded()}=${value.urlEncoded()}"
            }
        }

    private fun String.compactJson(): String =
        lines().joinToString(separator = "") { line -> line.trim() }

    private fun String.urlEncoded(): String =
        URLEncoder.encode(this, Charsets.UTF_8.name())

    private fun String?.resolveServerAssetUrl(): String? {
        val raw = this?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        if (raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true)) {
            return raw
        }
        val base = URL("$normalizedBaseUrl/")
        return URL(base, raw.removePrefix("/")).toString()
    }
}
