package io.github.jameslebone.autocontactbridge.ui.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private fun decodeOrientedBitmap(context: Context, uri: Uri): Bitmap? {
    return try {
        val orientation = context.contentResolver.openInputStream(uri)?.use { stream ->
            val exifInterface = ExifInterface(stream)
            exifInterface.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            )
        } ?: ExifInterface.ORIENTATION_NORMAL

        val rawBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream)
        } ?: return null

        val rotationDegrees = when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> 90f
            ExifInterface.ORIENTATION_ROTATE_180 -> 180f
            ExifInterface.ORIENTATION_ROTATE_270 -> 270f
            ExifInterface.ORIENTATION_TRANSPOSE -> 90f
            ExifInterface.ORIENTATION_TRANSVERSE -> 270f
            else -> 0f
        }

        if (rotationDegrees != 0f) {
            val matrix = Matrix().apply { postRotate(rotationDegrees) }
            Bitmap.createBitmap(rawBitmap, 0, 0, rawBitmap.width, rawBitmap.height, matrix, true)
        } else {
            rawBitmap
        }
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ContactAvatar(
    name: String,
    avatarUriString: String?,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    val context = LocalContext.current
    val bitmap = remember(avatarUriString) {
        if (!avatarUriString.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(avatarUriString)
                decodeOrientedBitmap(context, uri)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val initial = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
            Text(
                text = initial,
                style = if (size > 50.dp) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ContactAvatarPreview() {
    MaterialTheme {
        ContactAvatar(name = "John Doe", avatarUriString = null)
    }
}
