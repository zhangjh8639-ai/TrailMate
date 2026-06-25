package com.trailmate.app.core.auth

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TrailMateWechatManifestTest {
    @Test
    fun manifestAllowsQueryingWechatPackageForSdkLaunch() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(
            "WeChat SDK launch should be allowed to query com.tencent.mm on Android 11+.",
            manifest.contains("<queries>") &&
                manifest.contains("""<package android:name="com.tencent.mm" />""")
        )
    }

    @Test
    fun manifestRegistersWechatEntryActivityForCallbackReuse() {
        val manifest = File("src/main/AndroidManifest.xml").readText()

        assertTrue(manifest.contains("""android:name=".wxapi.WXEntryActivity""""))
        assertTrue(manifest.contains("""android:exported="true""""))
        assertTrue(manifest.contains("""android:launchMode="singleTop""""))
    }
}
