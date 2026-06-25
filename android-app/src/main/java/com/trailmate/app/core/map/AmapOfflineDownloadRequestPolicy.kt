package com.trailmate.app.core.map

enum class AmapOfflineDownloadRequestKind {
    CITY_CODE,
    CITY_NAME
}

data class AmapOfflineDownloadRequest(
    val kind: AmapOfflineDownloadRequestKind,
    val value: String
)

object AmapOfflineDownloadRequestPolicy {
    fun resolve(
        cityName: String,
        cityCode: String?
    ): AmapOfflineDownloadRequest =
        cityCode
            ?.takeIf { it.isNotBlank() }
            ?.let { code ->
                AmapOfflineDownloadRequest(
                    kind = AmapOfflineDownloadRequestKind.CITY_CODE,
                    value = code
                )
            } ?: AmapOfflineDownloadRequest(
            kind = AmapOfflineDownloadRequestKind.CITY_NAME,
            value = cityName
        )
}
