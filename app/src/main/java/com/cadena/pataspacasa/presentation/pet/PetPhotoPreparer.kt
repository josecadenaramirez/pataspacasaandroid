package com.cadena.pataspacasa.presentation.pet

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.cadena.pataspacasa.core.error.AppError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.max
import kotlin.math.roundToInt

data class PreparedPetPhoto(
    val bytes: ByteArray,
    val originalByteCount: Long?,
    val originalWidth: Int,
    val originalHeight: Int,
    val sentWidth: Int,
    val sentHeight: Int
)

/**
 * Reads a MediaStore image without decoding its full resolution, applies its EXIF orientation,
 * and produces the bounded JPEG consumed by the pet-photo multipart endpoint.
 */
suspend fun preparePetPhoto(context: Context, uri: Uri): PreparedPetPhoto =
    withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        try {
            val bounds = readBounds(resolver, uri)
            if (bounds.first <= 0 || bounds.second <= 0) throw AppError.InvalidPhotoType

            val orientation = readOrientation(resolver, uri)
            val sampleSize = calculateSampleSize(bounds.first, bounds.second)
            val decoded = resolver.openInputStream(uri)?.use { input ->
                BitmapFactory.decodeStream(
                    input,
                    null,
                    BitmapFactory.Options().apply {
                        inSampleSize = sampleSize
                        inPreferredConfig = Bitmap.Config.ARGB_8888
                    }
                )
            } ?: throw AppError.InvalidPhotoType

            var prepared = decoded
            try {
                applyExifOrientation(prepared, orientation).also { oriented ->
                    if (oriented !== prepared) {
                        prepared.recycle()
                        prepared = oriented
                    }
                }
                scaleToFit(prepared, MAX_IMAGE_DIMENSION).also { scaled ->
                    if (scaled !== prepared) {
                        prepared.recycle()
                        prepared = scaled
                    }
                }
                flattenTransparency(prepared).also { flattened ->
                    if (flattened !== prepared) {
                        prepared.recycle()
                        prepared = flattened
                    }
                }

                val output = compressWithinBudget(prepared)
                val result = PreparedPetPhoto(
                    bytes = output.bytes,
                    originalByteCount = queryByteCount(resolver, uri),
                    originalWidth = bounds.first,
                    originalHeight = bounds.second,
                    sentWidth = output.width,
                    sentHeight = output.height
                )
                Log.i(
                    LOG_TAG,
                    "Prepared pet photo: original=${result.originalByteCount ?: -1} bytes " +
                        "${result.originalWidth}x${result.originalHeight}, " +
                        "sent=${result.bytes.size} bytes ${result.sentWidth}x${result.sentHeight}"
                )
                result
            } finally {
                if (!prepared.isRecycled) prepared.recycle()
            }
        } catch (error: AppError) {
            throw error
        } catch (_: OutOfMemoryError) {
            throw AppError.PhotoUploadFailed
        } catch (_: Exception) {
            throw AppError.InvalidPhotoType
        }
    }

internal fun calculateSampleSize(width: Int, height: Int): Int {
    val longestSide = max(width, height)
    // Use ceiling division so a common 4032 px camera image is decoded at sample size 2 instead
    // of allocating the full-resolution bitmap just because it is slightly below 4096 px.
    return max(
        1,
        ((longestSide.toLong() + MAX_IMAGE_DIMENSION - 1) / MAX_IMAGE_DIMENSION).toInt()
    )
}

private fun readBounds(resolver: ContentResolver, uri: Uri): Pair<Int, Int> {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    val input = resolver.openInputStream(uri) ?: throw AppError.InvalidPhotoType
    input.use { BitmapFactory.decodeStream(it, null, options) }
    return options.outWidth to options.outHeight
}

private fun readOrientation(resolver: ContentResolver, uri: Uri): Int =
    runCatching {
        resolver.openInputStream(uri)?.use { input ->
            ExifInterface(input).getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        }
    }.getOrNull() ?: ExifInterface.ORIENTATION_NORMAL

private fun applyExifOrientation(source: Bitmap, orientation: Int): Bitmap {
    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.setScale(-1f, 1f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.setRotate(180f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> {
            matrix.setRotate(180f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.setRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.setRotate(90f)
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.setRotate(-90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.setRotate(-90f)
        else -> return source
    }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun scaleToFit(source: Bitmap, maxDimension: Int): Bitmap {
    val longestSide = max(source.width, source.height)
    if (longestSide <= maxDimension) return source
    val scale = maxDimension.toFloat() / longestSide
    return Bitmap.createScaledBitmap(
        source,
        (source.width * scale).roundToInt().coerceAtLeast(1),
        (source.height * scale).roundToInt().coerceAtLeast(1),
        true
    )
}

private fun flattenTransparency(source: Bitmap): Bitmap {
    if (!source.hasAlpha()) return source
    return Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888).also { target ->
        Canvas(target).apply {
            drawColor(Color.WHITE)
            drawBitmap(source, 0f, 0f, null)
        }
    }
}

private data class CompressedJpeg(val bytes: ByteArray, val width: Int, val height: Int)

private fun compressWithinBudget(source: Bitmap): CompressedJpeg {
    var working = source
    var ownsWorking = false
    try {
        while (true) {
            for (quality in JPEG_QUALITIES) {
                val output = ByteArrayOutputStream()
                if (!working.compress(Bitmap.CompressFormat.JPEG, quality, output)) {
                    throw AppError.PhotoUploadFailed
                }
                val bytes = output.toByteArray()
                if (bytes.size <= TARGET_JPEG_BYTES) {
                    return CompressedJpeg(bytes, working.width, working.height)
                }
            }

            if (max(working.width, working.height) <= MIN_IMAGE_DIMENSION) {
                throw AppError.PhotoTooLarge
            }
            val smaller = Bitmap.createScaledBitmap(
                working,
                (working.width * RESIZE_STEP).roundToInt().coerceAtLeast(1),
                (working.height * RESIZE_STEP).roundToInt().coerceAtLeast(1),
                true
            )
            if (ownsWorking) working.recycle()
            working = smaller
            ownsWorking = true
        }
    } finally {
        if (ownsWorking && !working.isRecycled) working.recycle()
    }
}

private fun queryByteCount(resolver: ContentResolver, uri: Uri): Long? {
    val descriptorLength = runCatching {
        resolver.openAssetFileDescriptor(uri, "r")?.use { it.length }
    }.getOrNull()?.takeIf { it >= 0 }
    if (descriptorLength != null) return descriptorLength

    return runCatching {
        resolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst() && !cursor.isNull(0)) cursor.getLong(0) else null
        }
    }.getOrNull()
}

private const val LOG_TAG = "PetPhoto"
private const val MAX_IMAGE_DIMENSION = 2048
private const val MIN_IMAGE_DIMENSION = 512
// QA's nginx rejects request bodies at 1 MiB. Keep enough room for multipart headers and boundary.
private const val TARGET_JPEG_BYTES = 900 * 1024
private const val RESIZE_STEP = 0.85f
private val JPEG_QUALITIES = intArrayOf(85, 80, 75, 70, 65, 60)
