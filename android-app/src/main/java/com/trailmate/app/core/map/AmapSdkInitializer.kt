package com.trailmate.app.core.map

import android.content.Context
import com.amap.api.maps.MapsInitializer
import java.io.File

object AmapSdkStorageDirectoryPolicy {
    fun resolve(
        externalFilesDirPath: String?,
        filesDirPath: String
    ): String =
        "${(externalFilesDirPath ?: filesDirPath).trimEnd('/')}/$AMAP_DIR_NAME"

    private const val AMAP_DIR_NAME = "amap"
}

object AmapSdkInitializer {
    fun initialize(context: Context): String {
        val appContext = context.applicationContext
        val storageDirectory = AmapSdkStorageDirectoryPolicy.resolve(
            externalFilesDirPath = appContext.getExternalFilesDir(null)?.path,
            filesDirPath = appContext.filesDir.path
        )
        File(storageDirectory).mkdirs()
        MapsInitializer.sdcardDir = storageDirectory
        MapsInitializer.updatePrivacyShow(appContext, true, true)
        MapsInitializer.updatePrivacyAgree(appContext, true)
        return storageDirectory
    }
}
