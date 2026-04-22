package com.solari.app.ui.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.solari.app.data.user.ProfileAvatarUpload
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

private const val AvatarMaxDimensionPx = 1024
private const val AvatarCompressionQuality = 90

fun compressAvatarForUpload(context: Context, uri: Uri): ProfileAvatarUpload {
    val source = ImageDecoder.createSource(context.contentResolver, uri)
    val decodedBitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
        val scale = minOf(
            AvatarMaxDimensionPx.toFloat() / info.size.width,
            AvatarMaxDimensionPx.toFloat() / info.size.height,
            1f
        )

        decoder.setTargetSize(
            (info.size.width * scale).roundToInt().coerceAtLeast(1),
            (info.size.height * scale).roundToInt().coerceAtLeast(1)
        )
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
    }

    val squareBitmap = decodedBitmap.centerCropSquare()
    val encodedAvatar = squareBitmap.encodeAvatar()

    if (squareBitmap !== decodedBitmap) {
        decodedBitmap.recycle()
    }
    squareBitmap.recycle()

    return ProfileAvatarUpload(
        fileName = "avatar_${System.currentTimeMillis()}.${encodedAvatar.extension}",
        mimeType = encodedAvatar.mimeType,
        bytes = encodedAvatar.bytes
    )
}

private fun Bitmap.centerCropSquare(): Bitmap {
    val side = minOf(width, height)
    val left = (width - side) / 2
    val top = (height - side) / 2
    return Bitmap.createBitmap(this, left, top, side, side)
}

private fun Bitmap.encodeAvatar(): EncodedAvatar {
    val output = ByteArrayOutputStream()
    val format: Bitmap.CompressFormat
    val mimeType: String
    val extension: String
    val bitmapToEncode: Bitmap

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        format = Bitmap.CompressFormat.WEBP_LOSSY
        mimeType = "image/webp"
        extension = "webp"
        bitmapToEncode = this
    } else {
        format = Bitmap.CompressFormat.JPEG
        mimeType = "image/jpeg"
        extension = "jpg"
        bitmapToEncode = flattenForJpeg()
    }

    check(bitmapToEncode.compress(format, AvatarCompressionQuality, output)) {
        "Failed to compress avatar"
    }

    if (bitmapToEncode !== this) {
        bitmapToEncode.recycle()
    }

    return EncodedAvatar(
        mimeType = mimeType,
        extension = extension,
        bytes = output.toByteArray()
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

private data class EncodedAvatar(
    val mimeType: String,
    val extension: String,
    val bytes: ByteArray
)
