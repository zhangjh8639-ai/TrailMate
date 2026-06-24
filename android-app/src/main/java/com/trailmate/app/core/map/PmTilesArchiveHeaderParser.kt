package com.trailmate.app.core.map

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

private const val PMTILES_HEADER_LENGTH = 127
private const val PMTILES_ROOT_DIRECTORY_LIMIT_BYTES = 16 * 1024L
private const val POSITION_SCALE = 10_000_000.0

enum class PmTilesArchiveHeaderError {
    TOO_SHORT,
    BAD_MAGIC,
    UNSUPPORTED_VERSION,
    ROOT_DIRECTORY_EMPTY,
    ROOT_DIRECTORY_OUT_OF_FIRST_16KIB,
    TILE_DATA_EMPTY,
    TILE_DATA_OUT_OF_FILE,
    INVALID_ZOOM_RANGE,
    INVALID_BOUNDS
}

enum class PmTilesTileType(val id: Int) {
    UNKNOWN(0),
    MVT_VECTOR_TILE(1);

    companion object {
        fun fromId(id: Int): PmTilesTileType =
            entries.firstOrNull { it.id == id } ?: UNKNOWN
    }
}

data class PmTilesLatLngBounds(
    val minLongitude: Double,
    val minLatitude: Double,
    val maxLongitude: Double,
    val maxLatitude: Double
) {
    fun intersects(other: PmTilesLatLngBounds): Boolean =
        minLongitude <= other.maxLongitude &&
            maxLongitude >= other.minLongitude &&
            minLatitude <= other.maxLatitude &&
            maxLatitude >= other.minLatitude

    fun isValid(): Boolean =
        minLongitude in -180.0..180.0 &&
            maxLongitude in -180.0..180.0 &&
            minLatitude in -90.0..90.0 &&
            maxLatitude in -90.0..90.0 &&
            minLongitude <= maxLongitude &&
            minLatitude <= maxLatitude
}

data class PmTilesArchiveHeader(
    val specVersion: Int,
    val rootDirectoryOffset: Long,
    val rootDirectoryLength: Long,
    val metadataOffset: Long,
    val metadataLength: Long,
    val tileDataOffset: Long,
    val tileDataLength: Long,
    val tileType: PmTilesTileType,
    val minZoom: Int,
    val maxZoom: Int,
    val bounds: PmTilesLatLngBounds
)

data class PmTilesArchiveInspection(
    val header: PmTilesArchiveHeader?,
    val error: PmTilesArchiveHeaderError?
)

object PmTilesArchiveHeaderParser {
    fun inspect(file: File): PmTilesArchiveInspection {
        val fileSize = if (file.isFile) file.length() else 0L
        if (fileSize < PMTILES_HEADER_LENGTH) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.TOO_SHORT)
        }
        val bytes = file.inputStream().use { input ->
            val buffer = ByteArray(PMTILES_HEADER_LENGTH)
            val count = input.read(buffer)
            if (count <= 0) ByteArray(0) else buffer.copyOf(count)
        }
        return inspect(bytes, fileSize)
    }

    fun inspect(bytes: ByteArray, fileSizeBytes: Long?): PmTilesArchiveInspection {
        if (bytes.size < PMTILES_HEADER_LENGTH) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.TOO_SHORT)
        }
        val magic = bytes.copyOfRange(0, 7).toString(Charsets.UTF_8)
        if (magic != "PMTiles") {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.BAD_MAGIC)
        }

        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        buffer.position(7)
        val version = buffer.get().toInt() and 0xff
        if (version != 3) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.UNSUPPORTED_VERSION)
        }

        val rootOffset = buffer.long
        val rootLength = buffer.long
        val metadataOffset = buffer.long
        val metadataLength = buffer.long
        buffer.long // leaf directory offset
        buffer.long // leaf directory length
        val tileDataOffset = buffer.long
        val tileDataLength = buffer.long
        repeat(3) { buffer.long }
        buffer.get() // clustered
        buffer.get() // internal compression
        buffer.get() // tile compression
        val tileType = PmTilesTileType.fromId(buffer.get().toInt() and 0xff)
        val minZoom = buffer.get().toInt() and 0xff
        val maxZoom = buffer.get().toInt() and 0xff
        val minPosition = buffer.position()
        val minLongitude = buffer.intAt(minPosition) / POSITION_SCALE
        val minLatitude = buffer.intAt(minPosition + 4) / POSITION_SCALE
        val maxLongitude = buffer.intAt(minPosition + 8) / POSITION_SCALE
        val maxLatitude = buffer.intAt(minPosition + 12) / POSITION_SCALE

        if (rootLength <= 0L) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.ROOT_DIRECTORY_EMPTY)
        }
        if (rootOffset + rootLength > PMTILES_ROOT_DIRECTORY_LIMIT_BYTES) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.ROOT_DIRECTORY_OUT_OF_FIRST_16KIB)
        }
        if (tileDataLength <= 0L) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.TILE_DATA_EMPTY)
        }
        if (fileSizeBytes != null && tileDataOffset + tileDataLength > fileSizeBytes) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.TILE_DATA_OUT_OF_FILE)
        }
        if (maxZoom < minZoom) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.INVALID_ZOOM_RANGE)
        }

        val bounds = PmTilesLatLngBounds(minLongitude, minLatitude, maxLongitude, maxLatitude)
        if (!bounds.isValid()) {
            return PmTilesArchiveInspection(null, PmTilesArchiveHeaderError.INVALID_BOUNDS)
        }

        return PmTilesArchiveInspection(
            header = PmTilesArchiveHeader(
                specVersion = version,
                rootDirectoryOffset = rootOffset,
                rootDirectoryLength = rootLength,
                metadataOffset = metadataOffset,
                metadataLength = metadataLength,
                tileDataOffset = tileDataOffset,
                tileDataLength = tileDataLength,
                tileType = tileType,
                minZoom = minZoom,
                maxZoom = maxZoom,
                bounds = bounds
            ),
            error = null
        )
    }

    private fun ByteBuffer.intAt(index: Int): Int =
        duplicate().order(ByteOrder.LITTLE_ENDIAN).getInt(index)
}
