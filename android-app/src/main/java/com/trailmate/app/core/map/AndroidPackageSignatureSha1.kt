package com.trailmate.app.core.map

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import java.security.MessageDigest

object AndroidPackageSignatureSha1 {
    fun formatDigest(signatureBytes: ByteArray): String =
        MessageDigest.getInstance("SHA-1")
            .digest(signatureBytes)
            .joinToString(":") { byte -> "%02X".format(byte.toInt() and 0xFF) }
}

object AndroidPackageSignatureSha1Reader {
    fun read(context: Context): String? =
        runCatching {
            val packageManager = context.packageManager
            val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                val info = packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNING_CERTIFICATES
                )
                val signingInfo = info.signingInfo ?: return@runCatching null
                if (signingInfo.hasMultipleSigners()) {
                    signingInfo.apkContentsSigners
                } else {
                    signingInfo.signingCertificateHistory
                }
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.GET_SIGNATURES
                ).signatures
            }

            signatures
                ?.firstOrNull()
                ?.toByteArray()
                ?.let(AndroidPackageSignatureSha1::formatDigest)
        }.getOrNull()
}
