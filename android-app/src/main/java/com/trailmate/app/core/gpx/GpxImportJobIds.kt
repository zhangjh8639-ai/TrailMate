package com.trailmate.app.core.gpx

object GpxImportJobIds {
    fun create(kind: GpxImportJobKind, nonce: Long): String {
        return "${kind.name.lowercase()}:$nonce"
    }
}
