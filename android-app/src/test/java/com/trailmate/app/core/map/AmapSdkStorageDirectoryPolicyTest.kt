package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AmapSdkStorageDirectoryPolicyTest {
    @Test
    fun usesAppSpecificExternalDirectoryForAmapOfflineStorage() {
        val path = AmapSdkStorageDirectoryPolicy.resolve(
            externalFilesDirPath = "/storage/emulated/0/Android/data/com.trailmate.app/files",
            filesDirPath = "/data/user/0/com.trailmate.app/files"
        )

        assertEquals("/storage/emulated/0/Android/data/com.trailmate.app/files/amap", path)
    }

    @Test
    fun fallsBackToInternalFilesDirectoryWhenExternalFilesDirIsUnavailable() {
        val path = AmapSdkStorageDirectoryPolicy.resolve(
            externalFilesDirPath = null,
            filesDirPath = "/data/user/0/com.trailmate.app/files"
        )

        assertEquals("/data/user/0/com.trailmate.app/files/amap", path)
    }
}
