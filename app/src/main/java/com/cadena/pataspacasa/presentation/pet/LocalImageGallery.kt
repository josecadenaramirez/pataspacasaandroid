package com.cadena.pataspacasa.presentation.pet

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.cadena.pataspacasa.presentation.theme.AppColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class LocalDeviceImage(
    val id: Long,
    val uri: Uri,
    val displayName: String
)

enum class ImageMediaAccess { FULL, PARTIAL, NONE }

sealed class LocalImageLoadResult {
    data class Success(val images: List<LocalDeviceImage>) : LocalImageLoadResult()
    data class Error(val message: String) : LocalImageLoadResult()
}

fun requiredImagePermissions(): Array<String> = when {
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
    )
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    else -> arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
}

fun currentImageMediaAccess(context: Context): ImageMediaAccess {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    ) {
        return ImageMediaAccess.FULL
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE &&
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        return ImageMediaAccess.PARTIAL
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    ) {
        return ImageMediaAccess.FULL
    }
    return ImageMediaAccess.NONE
}

suspend fun loadLocalDeviceImages(context: Context): LocalImageLoadResult = withContext(Dispatchers.IO) {
    if (currentImageMediaAccess(context) == ImageMediaAccess.NONE) {
        return@withContext LocalImageLoadResult.Error(
            "No hay permiso para leer las imágenes guardadas en este dispositivo."
        )
    }
    try {
        val images = buildList {
            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.DATE_ADDED
            )
            val selection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                "${MediaStore.Images.Media.IS_PENDING} = 0"
            } else {
                null
            }
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                projection,
                selection,
                null,
                "${MediaStore.Images.Media.DATE_ADDED} DESC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                while (cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    add(
                        LocalDeviceImage(
                            id = id,
                            uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
                            displayName = cursor.getString(nameColumn).orEmpty().ifBlank { "Foto" }
                        )
                    )
                }
            }
        }
        LocalImageLoadResult.Success(images)
    } catch (_: SecurityException) {
        LocalImageLoadResult.Error("El permiso para leer tus imágenes cambió. Vuelve a intentarlo.")
    } catch (_: Exception) {
        LocalImageLoadResult.Error("No pudimos cargar las imágenes guardadas en este dispositivo.")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalImageGallerySheet(
    images: List<LocalDeviceImage>,
    isLoading: Boolean,
    access: ImageMediaAccess,
    onDismiss: () -> Unit,
    onImageSelected: (LocalDeviceImage) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 320.dp, max = 680.dp)
        ) {
            Text(
                text = "Fotos del dispositivo",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
            if (access == ImageMediaAccess.PARTIAL) {
                Surface(
                    color = AppColors.Primary.copy(alpha = 0.10f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Mostrando únicamente las fotos que autorizaste.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
            when {
                isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
                images.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No encontramos imágenes guardadas en este dispositivo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AppColors.TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 104.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(images, key = { it.id }) { image ->
                        val context = LocalContext.current
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(image.uri)
                                .size(360)
                                .crossfade(true)
                                .build(),
                            contentDescription = image.displayName,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                                .clickable { onImageSelected(image) }
                                .semantics {
                                    contentDescription = "Seleccionar ${image.displayName}"
                                }
                        )
                    }
                }
            }
        }
    }
}
