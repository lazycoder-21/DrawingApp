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

fun AppCompatActivity.takePhotoFromCamera(block: (Bitmap) -> Unit): ActivityResultLauncher<Void> {
    return registerForActivityResult(ActivityResultContracts.TakePicturePreview()) {
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
    block: (Uri) -> Unit,
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
            block(uri)
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

private suspend fun loadPhotosFromExternalStorage(): List<SharedStoragePhoto> {
        return withContext(Dispatchers.IO) {
            val collection = sdk29AndUp {
                MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

            val projection = arrayOf(
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DISPLAY_NAME,
                MediaStore.Images.Media.WIDTH,
                MediaStore.Images.Media.HEIGHT,
            )
            val photos = mutableListOf<SharedStoragePhoto>()
            contentResolver.query(
                collection,
                projection,
                null,
                null,
                "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val displayNameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
                val widthColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.WIDTH)
                val heightColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.HEIGHT)

                while(cursor.moveToNext()) {
                    val id = cursor.getLong(idColumn)
                    val displayName = cursor.getString(displayNameColumn)
                    val width = cursor.getInt(widthColumn)
                    val height = cursor.getInt(heightColumn)
                    val contentUri = ContentUris.withAppendedId(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        id
                    )
                    photos.add(SharedStoragePhoto(id, displayName, width, height, contentUri))
                }
                photos.toList()
            } ?: listOf()
        }

        ContentObserved is used to observe external storage photos

        intentSenderLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { deletePhotoFromExternalStorage }
        private suspend fun deletePhotoFromExternalStorage(photoUri: Uri) {
        withContext(Dispatchers.IO) {
            try {
                contentResolver.delete(photoUri, null, null)
            } catch (e: SecurityException) {
                val intentSender = when {
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                        MediaStore.createDeleteRequest(contentResolver, listOf(photoUri)).intentSender
                    }
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                        val recoverableSecurityException = e as? RecoverableSecurityException
                        recoverableSecurityException?.userAction?.actionIntent?.intentSender
                    }
                    else -> null
                }
                intentSender?.let { sender ->
                    intentSenderLauncher.launch(
                        IntentSenderRequest.Builder(sender).build()
                    )
                }
            }
        }
    }
*/