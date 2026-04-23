package com.solari.app.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import com.solari.app.ui.models.CapturedMedia
import java.io.ByteArrayOutputStream
import java.io.File

private const val PostImageMaxDimensionPx = 1440
private const val PostImageCompressionQuality = 92

data class PreparedPostMedia(
    val previewUri: Uri,
    val contentType: String,
    val bytes: ByteArray,
    val width: Int,
    val height: Int,
    val durationMs: Long? = null,
    val isVideo: Boolean
)

fun preparePostMediaForUpload(
    context: Context,
    media: CapturedMedia
): Result<PreparedPostMedia> {
    return runCatching {
        if (media.isVideo) {
            prepareVideoForUpload(context, media)
        } else {
            prepareImageForUpload(context, media)
        }
    }
}

fun createCaptureCacheFile(
    context: Context,
    extension: String
): File {
    val normalizedExtension = extension.trimStart('.')
    return File(
        context.cacheDir,
        "solari_capture_${System.currentTimeMillis()}.$normalizedExtension"
    )
}

private fun prepareImageForUpload(
    context: Context,
    media: CapturedMedia
): PreparedPostMedia {
    val source = ImageDecoder.createSource(context.contentResolver, media.uri)
    val decodedBitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val scale = minOf(
            PostImageMaxDimensionPx.toFloat() / info.size.width,
            PostImageMaxDimensionPx.toFloat() / info.size.height,
            1f
        )
        decoder.setTargetSize(
            (info.size.width * scale).toInt().coerceAtLeast(1),
            (info.size.height * scale).toInt().coerceAtLeast(1)
        )
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }

    val squareBitmap = decodedBitmap.centerCropSquare()
    val encoded = squareBitmap.encodeForPost()

    if (squareBitmap !== decodedBitmap) {
        decodedBitmap.recycle()
    }
    squareBitmap.recycle()

    return PreparedPostMedia(
        previewUri = media.uri,
        contentType = encoded.mimeType,
        bytes = encoded.bytes,
        width = encoded.width,
        height = encoded.height,
        isVideo = false
    )
}

private fun prepareVideoForUpload(
    context: Context,
    media: CapturedMedia
): PreparedPostMedia {
    val bytes = context.openUriBytes(media.uri)
    val retriever = MediaMetadataRetriever()

    return try {
        retriever.setDataSource(context, media.uri)
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            ?.toIntOrNull()
            ?: throw IllegalStateException("Missing video width")
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            ?.toIntOrNull()
            ?: throw IllegalStateException("Missing video height")
        val durationMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            ?.toLongOrNull()
            ?: media.durationMs
            ?: throw IllegalStateException("Missing video duration")

        require(width == height) { "Video preview must be square before upload." }

        PreparedPostMedia(
            previewUri = media.uri,
            contentType = media.contentType,
            bytes = bytes,
            width = width,
            height = height,
            durationMs = durationMs,
            isVideo = true
        )
    } finally {
        retriever.release()
    }
}

private fun Context.openUriBytes(uri: Uri): ByteArray {
    contentResolver.openInputStream(uri)?.use { input ->
        return input.readBytes()
    }

    if (uri.scheme == "file") {
        return File(requireNotNull(uri.path) { "Missing file path" }).readBytes()
    }

    throw IllegalStateException("Failed to open media.")
}

private fun Bitmap.centerCropSquare(): Bitmap {
    val side = minOf(width, height)
    val left = (width - side) / 2
    val top = (height - side) / 2
    return Bitmap.createBitmap(this, left, top, side, side)
}

private fun Bitmap.encodeForPost(): EncodedPostImage {
    val output = ByteArrayOutputStream()
    val compressFormat: Bitmap.CompressFormat
    val mimeType: String
    val bitmapToEncode: Bitmap

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        compressFormat = Bitmap.CompressFormat.WEBP_LOSSY
        mimeType = "image/webp"
        bitmapToEncode = this
    } else {
        compressFormat = Bitmap.CompressFormat.JPEG
        mimeType = "image/jpeg"
        bitmapToEncode = flattenForJpeg()
    }

    check(bitmapToEncode.compress(compressFormat, PostImageCompressionQuality, output)) {
        "Failed to encode image."
    }

    if (bitmapToEncode !== this) {
        bitmapToEncode.recycle()
    }

    return EncodedPostImage(
        mimeType = mimeType,
        bytes = output.toByteArray(),
        width = width,
        height = height
    )
}

private fun Bitmap.flattenForJpeg(): Bitmap {
    if (!hasAlpha()) return this

    val flattenedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(flattenedBitmap)
    canvas.drawColor(Color.WHITE)
    canvas.drawBitmap(this, 0f, 0f, null)
    return flattenedBitmap
}

private data class EncodedPostImage(
    val mimeType: String,
    val bytes: ByteArray,
    val width: Int,
    val height: Int
)
