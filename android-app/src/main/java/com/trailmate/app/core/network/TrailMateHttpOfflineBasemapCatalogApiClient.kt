package com.trailmate.app.core.network

import com.trailmate.app.core.map.PmTilesLatLngBounds
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import org.json.JSONObject

class TrailMateHttpOfflineBasemapCatalogApiClient(
    private val baseUrl: String,
    private val connectTimeoutMillis: Int = 10_000,
    private val readTimeoutMillis: Int = 10_000
) : TrailMateOfflineBasemapCatalogApi {
    private val normalizedBaseUrl = baseUrl.trim().trimEnd('/')

    override fun listPmTilesBasemaps(
        routeBounds: PmTilesLatLngBounds
    ): TrailMateApiResult<List<TrailMatePmTilesBasemapCatalogItemDto>> =
        requestJson(
            path = TrailMateServerApiContract.Endpoints.offlineBasemapPmTilesCatalog,
            query = listOf(
                "minLongitude" to routeBounds.minLongitude.toString(),
                "minLatitude" to routeBounds.minLatitude.toString(),
                "maxLongitude" to routeBounds.maxLongitude.toString(),
                "maxLatitude" to routeBounds.maxLatitude.toString()
            ),
            parseSuccess = { json ->
                TrailMateJson.arrayOrEmpty(json).objectList().map { objectJson -> objectJson.toCatalogItem() }
            }
        )

    private fun <T> requestJson(
        path: String,
        query: List<Pair<String, String>>,
        parseSuccess: (String) -> T
    ): TrailMateApiResult<T> {
        val url = URL(normalizedBaseUrl + TrailMateServerApiContract.BASE_PATH + path + query.suffix())
        val connection = url.openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = connectTimeoutMillis
            connection.readTimeout = readTimeoutMillis
            connection.setRequestProperty("Accept", "application/json")

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

    private fun JSONObject.toCatalogItem(): TrailMatePmTilesBasemapCatalogItemDto =
        TrailMatePmTilesBasemapCatalogItemDto(
            packId = nullableString("packId").orEmpty(),
            regionName = nullableString("regionName").orEmpty(),
            downloadUrl = nullableString("downloadUrl").resolveServerAssetUrl().orEmpty(),
            sizeBytes = nullableLong("sizeBytes"),
            sha256 = nullableString("sha256"),
            tileType = nullableString("tileType").orEmpty(),
            minZoom = nullableInt("minZoom") ?: 0,
            maxZoom = nullableInt("maxZoom") ?: 0,
            minLongitude = nullableDouble("minLongitude") ?: 0.0,
            minLatitude = nullableDouble("minLatitude") ?: 0.0,
            maxLongitude = nullableDouble("maxLongitude") ?: 0.0,
            maxLatitude = nullableDouble("maxLatitude") ?: 0.0,
            attribution = nullableString("attribution").orEmpty(),
            source = nullableString("source").orEmpty()
        )

    private fun String.toApiError(statusCode: Int): TrailMateApiError =
        TrailMateJson.apiError(this, statusCode)

    private fun List<Pair<String, String>>.suffix(): String =
        joinToString(prefix = "?", separator = "&") { (key, value) ->
            "${key.urlEncoded()}=${value.urlEncoded()}"
        }

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
