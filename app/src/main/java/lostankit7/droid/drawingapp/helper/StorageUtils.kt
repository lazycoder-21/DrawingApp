package lostankit7.droid.drawingapp.helper

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

inline fun <T> sdk29OrAbove(onSdk29: () -> T): T? {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        onSdk29()
    } else null
}

fun AppCompatActivity.takePhotoFromCamera(block: (Bitmap) -> Unit) {
    registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
        block(it)
    }
}

fun AppCompatActivity.pickImageFromGallery(block: (Uri) -> Unit): ActivityResultLauncher<Intent> {
    return registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result?.resultCode == Activity.RESULT_OK && result.data != null) {
            result.data?.let { intent ->
                intent.data?.let { block(it) }
            }
        }
    }
}

val intentChooseSingleImage =
    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

val intentChooseMultipleImage by lazy {
    Intent(Intent.ACTION_GET_CONTENT).also {
        it.type = "image/*"
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }
}

fun Activity.savePhotoToInternalStorage(
    bmp: Bitmap,
    filename: String = System.currentTimeMillis().toString()
): Boolean {
    return try {
        openFileOutput("$filename.jpg", AppCompatActivity.MODE_APPEND).use { stream ->
            if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, stream)) {
                throw IOException("Couldn't save bitmap.")
            }
        }
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}

fun Activity.savePhotoToExternalStorage(
    bmp: Bitmap,
    displayName: String = System.currentTimeMillis().toString()
): Boolean {
    val imageCollection = sdk29OrAbove {
        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
    } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "$displayName.jpg")
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.WIDTH, bmp.width)
        put(MediaStore.Images.Media.HEIGHT, bmp.height)
    }
    return try {
        contentResolver.insert(imageCollection, contentValues)?.also { uri ->
            contentResolver.openOutputStream(uri).use { outputStream ->
                if (!bmp.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)) {
                    throw IOException("Couldn't save bitmap")
                }
            }
        } ?: throw IOException("Couldn't create MediaStore entry")
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}


/*
private suspend fun loadPhotosFromInternalStorage(): List<InternalStoragePhoto> {
    return withContext(Dispatchers.IO) {
        val files = filesDir.listFiles()
        files?.filter { it.canRead() && it.isFile && it.name.endsWith(".jpg") }?.map {
            val bytes = it.readBytes()
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            InternalStoragePhoto(it.name, bmp)
        } ?: listOf()
    }
}

*/