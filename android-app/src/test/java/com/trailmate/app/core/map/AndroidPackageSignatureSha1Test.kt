package com.trailmate.app.core.map

import org.junit.Assert.assertEquals
import org.junit.Test

class AndroidPackageSignatureSha1Test {
    @Test
    fun formatsSignatureBytesAsUppercaseColonSeparatedSha1() {
        val sha1 = AndroidPackageSignatureSha1.formatDigest("abc".toByteArray())

        assertEquals("A9:99:3E:36:47:06:81:6A:BA:3E:25:71:78:50:C2:6C:9C:D0:D8:9D", sha1)
    }
}
