package com.cadena.pataspacasa.presentation.pet

import android.content.ContentValues
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Random

@RunWith(AndroidJUnit4::class)
class PetPhotoPreparerInstrumentedTest {

    @Test
    fun cameraSizedImage_isSampledAndCompressedBeforeUpload() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val resolver = context.contentResolver
        val uri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "patas-camera-large.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "DCIM/Camera")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
        ) ?: error("Could not create the test image")

        try {
            val width = 4032
            val height = 3024
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val random = Random(413L)
            val rowsPerChunk = 32
            val pixels = IntArray(width * rowsPerChunk)
            var y = 0
            while (y < height) {
                val rowCount = minOf(rowsPerChunk, height - y)
                for (index in 0 until width * rowCount) {
                    pixels[index] = random.nextInt() or (0xFF shl 24)
                }
                bitmap.setPixels(pixels, 0, width, 0, y, width, rowCount)
                y += rowCount
            }
            resolver.openOutputStream(uri)?.use { output ->
                assertTrue(bitmap.compress(Bitmap.CompressFormat.JPEG, 100, output))
            } ?: error("Could not write the test image")
            bitmap.recycle()
            resolver.update(
                uri,
                ContentValues().apply { put(MediaStore.Images.Media.IS_PENDING, 0) },
                null,
                null
            )

            val prepared = preparePetPhoto(context, uri)
            Log.i(
                "PetPhotoTest",
                "original=${prepared.originalByteCount} sent=${prepared.bytes.size} " +
                    "dimensions=${prepared.originalWidth}x${prepared.originalHeight}->" +
                    "${prepared.sentWidth}x${prepared.sentHeight}"
            )

            assertTrue((prepared.originalByteCount ?: 0L) > 5L * 1024 * 1024)
            assertTrue(prepared.bytes.size <= 900 * 1024)
            assertTrue(maxOf(prepared.sentWidth, prepared.sentHeight) <= 2048)
            assertEquals(0xFF.toByte(), prepared.bytes[0])
            assertEquals(0xD8.toByte(), prepared.bytes[1])
            assertEquals(0xFF.toByte(), prepared.bytes[2])
        } finally {
            resolver.delete(uri, null, null)
        }
    }
}
