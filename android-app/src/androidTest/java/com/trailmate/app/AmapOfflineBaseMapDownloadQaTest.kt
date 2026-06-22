package com.trailmate.app

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.test.platform.app.InstrumentationRegistry
import com.trailmate.app.BuildConfig
import com.amap.api.maps.offlinemap.OfflineMapCity
import com.amap.api.maps.offlinemap.OfflineMapManager
import com.amap.api.maps.offlinemap.OfflineMapStatus
import com.trailmate.app.core.map.AmapSdkInitializer
import com.trailmate.app.core.map.AmapOfflineDownloadRequestKind
import com.trailmate.app.core.map.AmapOfflineDownloadRequestPolicy
import com.trailmate.app.core.map.AmapOfflineDownloadQaDiagnostic
import com.trailmate.app.core.map.AmapOfflineDownloadQaDiagnosticEngine
import com.trailmate.app.core.map.AmapOfflineDownloadQaSnapshot
import com.trailmate.app.core.map.AmapOfflineBaseMapStatusReader
import com.trailmate.app.core.map.AndroidPackageSignatureSha1Reader
import java.io.File
import java.util.Locale
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class AmapOfflineBaseMapDownloadQaTest {
    @Test
    fun downloadsTargetCityWhenExplicitlyEnabled() {
        val instrumentation = InstrumentationRegistry.getInstrumentation()
        val arguments = InstrumentationRegistry.getArguments()
        assumeTrue(arguments.getString(ARG_ENABLE_DOWNLOAD_QA) == "true")

        val targetCityName = arguments.getString(ARG_CITY_NAME) ?: DEFAULT_TARGET_CITY
        val timeoutMs = arguments.getString(ARG_TIMEOUT_MS)?.toLongOrNull() ?: DEFAULT_TIMEOUT_MS
        val context = instrumentation.targetContext
        val runtimePackageSha1 = AndroidPackageSignatureSha1Reader.read(context)
        val offlineCatalogLoaded = CountDownLatch(1)
        val completed = CountDownLatch(1)
        var lastStatus = Int.MIN_VALUE
        var lastCompleteCode = 0
        var lastCityName = ""

        val amapStorageDirectory = AmapSdkInitializer.initialize(context)
        val manager = OfflineMapManager(
            context.applicationContext,
            object : OfflineMapManager.OfflineMapDownloadListener {
                override fun onDownload(status: Int, completeCode: Int, cityName: String?) {
                    lastStatus = status
                    lastCompleteCode = completeCode
                    lastCityName = cityName.orEmpty()
                    if (status == OfflineMapStatus.SUCCESS || completeCode >= COMPLETE_PERCENT) {
                        completed.countDown()
                    }
                }

                override fun onCheckUpdate(hasNew: Boolean, name: String?) = Unit
                override fun onRemove(success: Boolean, name: String?, describe: String?) = Unit
            }
        )
        manager.setOnOfflineLoadedListener {
            offlineCatalogLoaded.countDown()
        }

        try {
            val catalogVerified = offlineCatalogLoaded.await(
                CATALOG_VERIFY_TIMEOUT_MS,
                TimeUnit.MILLISECONDS
            )
            val networkValidated = context.hasValidatedNetwork()
            val city = manager.resolveTargetCity(targetCityName)
            val initialDownloadedRegions = AmapOfflineBaseMapStatusReader
                .readDownloadedStatus(context)
                ?.downloadedRegionCount ?: 0
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

            val resolvedCity = city
            val request = AmapOfflineDownloadRequestPolicy.resolve(
                cityName = resolvedCity.city.orEmpty(),
                cityCode = resolvedCity.code
            )
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
            if (!resolvedCity.hasDownloadedState()) {
                when (request.kind) {
                    AmapOfflineDownloadRequestKind.CITY_CODE -> manager.downloadByCityCode(request.value)
                    AmapOfflineDownloadRequestKind.CITY_NAME -> manager.downloadByCityName(request.value)
                }
                completed.await(timeoutMs, TimeUnit.MILLISECONDS)
            }

            val refreshedCity = manager.resolveTargetCity(resolvedCity.city)
            val downloadedRegions = AmapOfflineBaseMapStatusReader
                .readDownloadedStatus(context)
                ?.downloadedRegionCount ?: 0
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
                    "refreshed=${refreshedCity?.statusSummary()}",
                diagnostic.passed
            )
        } finally {
            manager.destroy()
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
            "complete=${getcompleteCode()}"

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
        const val ARG_TIMEOUT_MS = "trailmateOfflineDownloadTimeoutMs"
        const val DEFAULT_TARGET_CITY = "杭州市"
        const val DEFAULT_TIMEOUT_MS = 10 * 60 * 1_000L
        const val CATALOG_VERIFY_TIMEOUT_MS = 60 * 1_000L
        const val COMPLETE_PERCENT = 100
    }
}
