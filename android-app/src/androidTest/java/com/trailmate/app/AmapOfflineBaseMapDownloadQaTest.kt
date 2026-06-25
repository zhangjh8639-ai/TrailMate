package com.trailmate.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.test.platform.app.InstrumentationRegistry
import com.trailmate.app.BuildConfig
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapStatus
import com.trailmate.app.core.map.AmapSdkInitializer
import com.trailmate.app.core.map.AmapOfflineDownloadRequest
import com.trailmate.app.core.map.AmapOfflineDownloadRequestKind
import com.trailmate.app.core.map.AmapOfflineDownloadRequestPolicy
import com.trailmate.app.core.map.AmapOfflineDownloadQaDiagnostic
import com.trailmate.app.core.map.AmapOfflineDownloadQaDiagnosticEngine
import com.trailmate.app.core.map.AmapOfflineDownloadQaSnapshot
import com.trailmate.app.core.map.AmapOfflineBaseMapStatusReader
import com.trailmate.app.core.map.AndroidPackageSignatureSha1Reader
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Collections
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class AmapOfflineBaseMapDownloadQaTest {
    @Test(timeout = OVERALL_TEST_TIMEOUT_MS)
    fun downloadsTargetCityWhenExplicitlyEnabled() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString(ARG_ENABLE_DOWNLOAD_QA) == "true")

        val targetCityName = arguments.getString(ARG_CITY_NAME) ?: DEFAULT_TARGET_CITY
        val timeoutMs = arguments.getString(ARG_TIMEOUT_MS)?.toLongOrNull() ?: DEFAULT_TIMEOUT_MS
        val resetBeforeDownload = arguments.getString(ARG_RESET_BEFORE_DOWNLOAD) == "true"
        val updateBeforeDownload = arguments.getString(ARG_UPDATE_BEFORE_DOWNLOAD) == "true"
        val probeCityPackApi = arguments.getString(ARG_PROBE_CITY_PACK_API) == "true"
        Log.i(
            TAG,
            "qa.start targetCity=$targetCityName timeoutMs=$timeoutMs " +
                "resetBeforeDownload=$resetBeforeDownload updateBeforeDownload=$updateBeforeDownload " +
                "probeCityPackApi=$probeCityPackApi"
        )
        val context = instrumentation.targetContext
        Log.i(TAG, "qa.context package=${context.packageName}")
        val runtimePackageSha1 = AndroidPackageSignatureSha1Reader.read(context)
        val offlineCatalogLoaded = CountDownLatch(1)
        val completed = CountDownLatch(1)
        val removeCompleted = CountDownLatch(1)
        val checkUpdateCompleted = CountDownLatch(1)
        var lastStatus = Int.MIN_VALUE
        var lastCompleteCode = 0
        var lastCityName = ""
        val callbackEvents = Collections.synchronizedList(mutableListOf<String>())

        Log.i(TAG, "qa.initialize.begin")
        val amapStorageDirectory = AmapSdkInitializer.initialize(context)
        Log.i(TAG, "qa.initialize.end storage=$amapStorageDirectory")
        Log.i(TAG, "qa.manager.create.begin")
        val manager = OfflineMapManager(
            context.applicationContext,
            object : OfflineMapManager.OfflineMapDownloadListener {
                override fun onDownload(status: Int, completeCode: Int, cityName: String?) {
                    lastStatus = status
                    lastCompleteCode = completeCode
                    lastCityName = cityName.orEmpty()
                    callbackEvents.add(
                        "onDownload(status=${status.offlineStatusLabel()}, " +
                            "complete=$completeCode, city=${cityName.orEmpty()})"
                    )
                    if (status == OfflineMapStatus.SUCCESS || completeCode >= COMPLETE_PERCENT) {
                        completed.countDown()
                    }
                }

                override fun onCheckUpdate(hasNew: Boolean, name: String?) {
                    callbackEvents.add("onCheckUpdate(hasNew=$hasNew, name=${name.orEmpty()})")
                    checkUpdateCompleted.countDown()
                }

                override fun onRemove(success: Boolean, name: String?, describe: String?) {
                    callbackEvents.add(
                        "onRemove(success=$success, name=${name.orEmpty()}, " +
                            "describe=${describe.orEmpty()})"
                    )
                    removeCompleted.countDown()
                }
            }
        )
        Log.i(TAG, "qa.manager.create.end")
        manager.setOnOfflineLoadedListener {
            Log.i(TAG, "qa.catalog.loaded")
            offlineCatalogLoaded.countDown()
        }

        try {
            Log.i(TAG, "qa.catalog.await.begin")
            val catalogVerified = offlineCatalogLoaded.await(
                CATALOG_VERIFY_TIMEOUT_MS,
                TimeUnit.MILLISECONDS
            )
            Log.i(TAG, "qa.catalog.await.end loaded=$catalogVerified")
            Log.i(TAG, "qa.network.begin")
            val networkValidated = context.hasValidatedNetwork()
            Log.i(TAG, "qa.network.end validated=$networkValidated")
            Log.i(TAG, "qa.resolveCity.begin")
            val city = manager.resolveTargetCity(targetCityName)
            Log.i(TAG, "qa.resolveCity.end city=${city?.statusSummary()}")
            Log.i(TAG, "qa.initialDownloadedRegions.begin")
            val initialDownloadedRegions = AmapOfflineBaseMapStatusReader
                .readDownloadedStatus(context)
                ?.downloadedRegionCount ?: 0
            Log.i(TAG, "qa.initialDownloadedRegions.end count=$initialDownloadedRegions")
            if (city == null) {
                val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
                    AmapOfflineDownloadQaSnapshot(
                        targetCityName = targetCityName,
                        amapKeyConfigured = BuildConfig.TRAILMATE_AMAP_API_KEY.isNotBlank(),
                        runtimePackageName = context.packageName,
                        runtimePackageSha1 = runtimePackageSha1,
                        catalogLoaded = catalogVerified,
                        targetCityResolved = false,
                        resolvedCityName = "",
                        resolvedCityCode = null,
                        resolvedAdcode = null,
                        downloadRequestKind = null,
                        downloadRequestValue = null,
                        amapStorageDirectory = amapStorageDirectory,
                        initialDownloadedRegionCount = initialDownloadedRegions,
                        downloadedRegionCount = initialDownloadedRegions,
                        downloadedStateVerified = false,
                        downloadedRegionCountIncreased = false,
                        lastStatusLabel = lastStatus.offlineStatusLabel(),
                        lastCompletePercent = lastCompleteCode,
                        lastCallbackCityName = lastCityName,
                        networkValidated = networkValidated,
                        amapStorageWritable = amapStorageDirectory.isWritableDirectory()
                    )
                )
                assertTrue(diagnostic.failureMessage(), diagnostic.passed)
                return
            }

            var resolvedCity = city
            if (resetBeforeDownload) {
                Log.i(TAG, "qa.remove.start city=${resolvedCity.city.orEmpty()}")
                manager.remove(resolvedCity.city.orEmpty())
                val removeReturned = removeCompleted.await(
                    REMOVE_VERIFY_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS
                )
                Log.i(TAG, "qa.remove.end callback=$removeReturned")
                resolvedCity = manager.resolveTargetCity(targetCityName) ?: resolvedCity
                Log.i(TAG, "qa.remove.refreshedCity city=${resolvedCity.statusSummary()}")
            }
            val request = when (arguments.getString(ARG_DOWNLOAD_REQUEST_KIND)?.uppercase(Locale.ROOT)) {
                AmapOfflineDownloadRequestKind.CITY_NAME.name -> AmapOfflineDownloadRequest(
                    kind = AmapOfflineDownloadRequestKind.CITY_NAME,
                    value = resolvedCity.city.orEmpty()
                )

                AmapOfflineDownloadRequestKind.CITY_CODE.name -> AmapOfflineDownloadRequest(
                    kind = AmapOfflineDownloadRequestKind.CITY_CODE,
                    value = resolvedCity.code.orEmpty()
                )

                else -> AmapOfflineDownloadRequestPolicy.resolve(
                    cityName = resolvedCity.city.orEmpty(),
                    cityCode = resolvedCity.code
                )
            }
            if (!networkValidated) {
                val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
                    AmapOfflineDownloadQaSnapshot(
                        targetCityName = targetCityName,
                        amapKeyConfigured = BuildConfig.TRAILMATE_AMAP_API_KEY.isNotBlank(),
                        runtimePackageName = context.packageName,
                        runtimePackageSha1 = runtimePackageSha1,
                        catalogLoaded = catalogVerified,
                        targetCityResolved = true,
                        resolvedCityName = resolvedCity.city.orEmpty(),
                        resolvedCityCode = resolvedCity.code,
                        resolvedAdcode = resolvedCity.adcode,
                        downloadRequestKind = request.kind.name,
                        downloadRequestValue = request.value,
                        amapStorageDirectory = amapStorageDirectory,
                        initialDownloadedRegionCount = initialDownloadedRegions,
                        downloadedRegionCount = initialDownloadedRegions,
                        downloadedStateVerified = resolvedCity.hasDownloadedState(),
                        downloadedRegionCountIncreased = false,
                        lastStatusLabel = lastStatus.offlineStatusLabel(),
                        lastCompletePercent = lastCompleteCode,
                        lastCallbackCityName = lastCityName,
                        networkValidated = false,
                        amapStorageWritable = amapStorageDirectory.isWritableDirectory()
                    )
                )
                assertTrue(
                    diagnostic.failureMessage() +
                        ", resolved=${resolvedCity.statusSummary()}",
                    diagnostic.passed
                )
                return
            }
            val beforeDownloadLists = manager.offlineListSummary(resolvedCity.city)
            Log.i(TAG, "qa.beforeDownloadLists $beforeDownloadLists")
            val cityPackApiProbe = if (probeCityPackApi) {
                probeAmapCityPackApi(context, resolvedCity.adcode)
            } else {
                "skipped"
            }
            Log.i(TAG, "qa.cityPackApiProbe $cityPackApiProbe")
            if (updateBeforeDownload) {
                val updateError = runCatching {
                    when (request.kind) {
                        AmapOfflineDownloadRequestKind.CITY_CODE -> manager.updateOfflineCityByCode(request.value)
                        AmapOfflineDownloadRequestKind.CITY_NAME -> manager.updateOfflineCityByName(request.value)
                    }
                }.exceptionOrNull()?.let { exception ->
                    "${exception::class.java.simpleName}:${exception.message.orEmpty()}"
                }
                Log.i(TAG, "qa.update.started error=${updateError.orEmpty()}")
                val checkUpdateReturned = checkUpdateCompleted.await(
                    CHECK_UPDATE_VERIFY_TIMEOUT_MS,
                    TimeUnit.MILLISECONDS
                )
                Log.i(TAG, "qa.update.end callback=$checkUpdateReturned")
            }
            var downloadStartError: String? = null
            if (!resolvedCity.hasDownloadedState()) {
                Log.i(TAG, "qa.download.start kind=${request.kind} value=${request.value}")
                downloadStartError = runCatching {
                    when (request.kind) {
                        AmapOfflineDownloadRequestKind.CITY_CODE -> manager.downloadByCityCode(request.value)
                        AmapOfflineDownloadRequestKind.CITY_NAME -> manager.downloadByCityName(request.value)
                    }
                }.exceptionOrNull()?.let { exception ->
                    "${exception::class.java.simpleName}:${exception.message.orEmpty()}"
                }
                Log.i(TAG, "qa.download.started error=${downloadStartError.orEmpty()}")
                if (downloadStartError == null) {
                    Log.i(TAG, "qa.download.await.begin")
                    completed.await(timeoutMs, TimeUnit.MILLISECONDS)
                    Log.i(TAG, "qa.download.await.end")
                }
            }

            Log.i(TAG, "qa.refreshedCity.begin")
            val refreshedCity = manager.resolveTargetCity(resolvedCity.city)
            Log.i(TAG, "qa.refreshedCity.end city=${refreshedCity?.statusSummary()}")
            val afterDownloadLists = manager.offlineListSummary(resolvedCity.city)
            Log.i(TAG, "qa.afterDownloadLists $afterDownloadLists")
            Log.i(TAG, "qa.downloadedRegions.begin")
            val downloadedRegions = AmapOfflineBaseMapStatusReader
                .readDownloadedStatus(context)
                ?.downloadedRegionCount ?: 0
            Log.i(TAG, "qa.downloadedRegions.end count=$downloadedRegions")
            val diagnostic = AmapOfflineDownloadQaDiagnosticEngine.evaluate(
                AmapOfflineDownloadQaSnapshot(
                    targetCityName = targetCityName,
                    amapKeyConfigured = BuildConfig.TRAILMATE_AMAP_API_KEY.isNotBlank(),
                    runtimePackageName = context.packageName,
                    runtimePackageSha1 = runtimePackageSha1,
                    catalogLoaded = catalogVerified,
                    targetCityResolved = true,
                    resolvedCityName = resolvedCity.city.orEmpty(),
                    resolvedCityCode = resolvedCity.code,
                    resolvedAdcode = resolvedCity.adcode,
                    downloadRequestKind = request.kind.name,
                    downloadRequestValue = request.value,
                    amapStorageDirectory = amapStorageDirectory,
                    initialDownloadedRegionCount = initialDownloadedRegions,
                    downloadedRegionCount = downloadedRegions,
                    downloadedStateVerified = refreshedCity?.hasDownloadedState() == true,
                    downloadedRegionCountIncreased = downloadedRegions > initialDownloadedRegions,
                    lastStatusLabel = lastStatus.offlineStatusLabel(),
                    lastCompletePercent = lastCompleteCode,
                    lastCallbackCityName = lastCityName,
                    networkValidated = networkValidated,
                    amapStorageWritable = amapStorageDirectory.isWritableDirectory()
                )
            )
            assertTrue(
                diagnostic.failureMessage() +
                    ", resolved=${resolvedCity.statusSummary()}, " +
                    "refreshed=${refreshedCity?.statusSummary()}, " +
                    "downloadStartError=${downloadStartError.orEmpty()}, " +
                    "cityPackApiProbe=$cityPackApiProbe, " +
                    "callbacks=${callbackEvents.snapshotEvents()}, " +
                    "beforeLists=$beforeDownloadLists, afterLists=$afterDownloadLists",
                diagnostic.passed
            )
        } finally {
            Log.i(TAG, "qa.manager.destroy.begin")
            manager.destroy()
            Log.i(TAG, "qa.manager.destroy.end")
        }
    }

    private fun OfflineMapManager.resolveTargetCity(targetCityName: String): OfflineMapCity? {
        val normalizedTarget = targetCityName.normalizedCityName()
        val aliases = listOf(
            targetCityName,
            targetCityName.removeSuffix("市"),
            "$targetCityName 市".replace(" ", "")
        ).distinct()

        aliases.forEach { alias ->
            getItemByCityName(alias)?.let { return it }
        }

        return getOfflineMapCityList()
            .orEmpty()
            .firstOrNull { city ->
                val normalizedCity = city.city.normalizedCityName()
                normalizedCity == normalizedTarget ||
                    normalizedCity.contains(normalizedTarget) ||
                    normalizedTarget.contains(normalizedCity) ||
                    city.pinyin.equals(normalizedTarget, ignoreCase = true) ||
                    city.jianpin.equals(normalizedTarget, ignoreCase = true)
            }
    }

    private fun OfflineMapCity.hasDownloadedState(): Boolean =
        state == OfflineMapStatus.SUCCESS || getcompleteCode() >= COMPLETE_PERCENT

    private fun OfflineMapCity.statusSummary(): String =
        "city=$city, code=$code, adcode=$adcode, state=${state.offlineStatusLabel()}, " +
            "complete=${getcompleteCode()}, size=${debugSize()}, url=${debugUrl()}"

    private fun OfflineMapCity.debugSize(): String =
        runCatching { getSize().toString() }
            .getOrElse { exception -> "ERROR:${exception::class.java.simpleName}" }

    private fun OfflineMapCity.debugUrl(): String =
        runCatching {
            val url = getUrl().orEmpty()
            if (url.length <= MAX_DEBUG_URL_LENGTH) {
                url
            } else {
                url.take(MAX_DEBUG_URL_LENGTH) + "..."
            }
        }.getOrElse { exception -> "ERROR:${exception::class.java.simpleName}" }

    private fun probeAmapCityPackApi(context: Context, adcode: String?): String {
        if (adcode.isNullOrBlank()) {
            return "skipped:no-adcode"
        }
        return runCatching {
            val handlerClass = Class.forName("com.amap.api.col.3sl.j2")
            val handler = handlerClass
                .getConstructor(Context::class.java, String::class.java)
                .newInstance(context.applicationContext, adcode)
            val url = handlerClass.getMethod("getURL").invoke(handler) as String
            @Suppress("UNCHECKED_CAST")
            val params = handlerClass.getMethod("getParams").invoke(handler) as Map<String, String>
            val requestUrl = "$url?${params.toQueryString()}"
            "url=$url, params=${params.redactedSummary()}, response=${requestUrl.httpGetSnippet()}"
        }.getOrElse { exception ->
            "ERROR:${exception::class.java.simpleName}:${exception.message.orEmpty()}"
        }
    }

    private fun Map<String, String>.toQueryString(): String =
        entries.joinToString("&") { (key, value) ->
            "${key.urlEncode()}=${value.urlEncode()}"
        }

    private fun Map<String, String>.redactedSummary(): String =
        entries
            .sortedBy { entry -> entry.key }
            .joinToString(",") { (key, value) ->
                when (key) {
                    "key", "scode" -> "$key=<set:${value.length}>"
                    else -> "$key=$value"
                }
            }

    private fun String.urlEncode(): String =
        URLEncoder.encode(this, "UTF-8")

    private fun String.httpGetSnippet(): String {
        val connection = URL(this).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = API_PROBE_TIMEOUT_MS
        connection.readTimeout = API_PROBE_TIMEOUT_MS
        return try {
            val code = connection.responseCode
            val stream = if (code in HTTP_SUCCESS_RANGE) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream
                ?.bufferedReader()
                ?.use { reader -> reader.readText() }
                .orEmpty()
                .compactSnippet()
            "code=$code, body=$body"
        } finally {
            connection.disconnect()
        }
    }

    private fun String.compactSnippet(): String =
        redactAmapSecretFields()
            .replace(Regex("\\s+"), " ")
            .take(MAX_DEBUG_BODY_LENGTH)

    private fun String.redactAmapSecretFields(): String =
        replace(Regex("\"(key|sec_code|sec_code_debug)\"\\s*:\\s*\"[^\"]*\"")) { match ->
            "\"${match.groups[1]?.value.orEmpty()}\":\"<redacted>\""
        }

    private fun OfflineMapManager.offlineListSummary(cityName: String?): String {
        val normalizedCity = cityName.orEmpty().normalizedCityName()
        return "catalog=${getOfflineMapCityList().orEmpty().matchingCities(normalizedCity)}, " +
            "downloading=${getDownloadingCityList().orEmpty().matchingCities(normalizedCity)}, " +
            "downloaded=${getDownloadOfflineMapCityList().orEmpty().matchingCities(normalizedCity)}"
    }

    private fun List<OfflineMapCity>.matchingCities(normalizedCity: String): String {
        val matches = filter { city ->
            val normalizedCandidate = city.city.normalizedCityName()
            normalizedCandidate == normalizedCity ||
                normalizedCandidate.contains(normalizedCity) ||
                normalizedCity.contains(normalizedCandidate)
        }.take(MAX_DEBUG_CITY_COUNT)
        return if (matches.isEmpty()) {
            "none"
        } else {
            matches.joinToString(separator = " | ") { it.statusSummary() }
        }
    }

    private fun List<String>.snapshotEvents(): String =
        synchronized(this) {
            if (isEmpty()) {
                "none"
            } else {
                takeLast(MAX_DEBUG_CALLBACK_COUNT).joinToString(separator = " || ")
            }
        }

    private fun Int.offlineStatusLabel(): String =
        when (this) {
            Int.MIN_VALUE -> "NO_CALLBACK"
            OfflineMapStatus.ERROR -> "ERROR(-1)"
            OfflineMapStatus.LOADING -> "LOADING(0)"
            OfflineMapStatus.UNZIP -> "UNZIP(1)"
            OfflineMapStatus.WAITING -> "WAITING(2)"
            OfflineMapStatus.PAUSE -> "PAUSE(3)"
            OfflineMapStatus.SUCCESS -> "SUCCESS(4)"
            OfflineMapStatus.STOP -> "STOP(5)"
            OfflineMapStatus.CHECKUPDATES -> "CHECKUPDATES(6)"
            OfflineMapStatus.NEW_VERSION -> "NEW_VERSION(7)"
            OfflineMapStatus.EXCEPTION_NETWORK_LOADING -> "EXCEPTION_NETWORK_LOADING(101)"
            OfflineMapStatus.EXCEPTION_AMAP -> "EXCEPTION_AMAP(102)"
            OfflineMapStatus.EXCEPTION_SDCARD -> "EXCEPTION_SDCARD(103)"
            OfflineMapStatus.START_DOWNLOAD_FAILED -> "START_DOWNLOAD_FAILED(1002)"
            else -> "UNKNOWN($this)"
        }

    private fun String.normalizedCityName(): String =
        trim()
            .removeSuffix("市")
            .lowercase(Locale.ROOT)

    private fun Context.hasValidatedNetwork(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun String.isWritableDirectory(): Boolean {
        val probe = runCatching {
            val directory = File(this)
            directory.mkdirs()
            val file = File.createTempFile("amap-offline-qa", ".tmp", directory)
            file.writeText("ok")
            file.delete()
        }
        return probe.isSuccess
    }

    private fun AmapOfflineDownloadQaDiagnostic.failureMessage(): String =
        "AMap offline map did not finish. " +
            "status=$statusLabel, blockers=${blockers.joinToString(" | ")}, " +
            "next=$nextActionLabel, recovery=$recoveryAction, " +
            "steps=${recoverySteps.joinToString(" -> ")}, summary=$summary"

    private companion object {
        const val ARG_ENABLE_DOWNLOAD_QA = "trailmateOfflineDownloadQa"
        const val ARG_CITY_NAME = "trailmateOfflineCityName"
        const val ARG_DOWNLOAD_REQUEST_KIND = "trailmateOfflineDownloadRequestKind"
        const val ARG_TIMEOUT_MS = "trailmateOfflineDownloadTimeoutMs"
        const val ARG_RESET_BEFORE_DOWNLOAD = "trailmateOfflineResetBeforeDownload"
        const val ARG_UPDATE_BEFORE_DOWNLOAD = "trailmateOfflineUpdateBeforeDownload"
        const val ARG_PROBE_CITY_PACK_API = "trailmateOfflineProbeCityPackApi"
        const val DEFAULT_TARGET_CITY = "杭州市"
        const val DEFAULT_TIMEOUT_MS = 10 * 60 * 1_000L
        const val API_PROBE_TIMEOUT_MS = 12 * 1_000
        const val CATALOG_VERIFY_TIMEOUT_MS = 60 * 1_000L
        const val REMOVE_VERIFY_TIMEOUT_MS = 30 * 1_000L
        const val CHECK_UPDATE_VERIFY_TIMEOUT_MS = 30 * 1_000L
        const val COMPLETE_PERCENT = 100
        const val MAX_DEBUG_CALLBACK_COUNT = 40
        const val MAX_DEBUG_BODY_LENGTH = 800
        const val MAX_DEBUG_CITY_COUNT = 5
        const val MAX_DEBUG_URL_LENGTH = 180
        const val OVERALL_TEST_TIMEOUT_MS = 180 * 1_000L
        const val TAG = "TrailMateAmapQa"
        val HTTP_SUCCESS_RANGE = 200..399
    }
}
