package lostankit7.droid.drawingapp.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import lostankit7.droid.drawingapp.R


val intentChooseSingleImage =
    Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)

val intentChooseMultipleImage by lazy {
    Intent(Intent.ACTION_GET_CONTENT).also {
        it.type = "image/*"
        it.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
    }
}

fun Activity.showSnackBar(message: String, snackBarColor: Int = R.color.snackBar_error) {
    val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
    snackBar.view.setBackgroundColor(ContextCompat.getColor(this, snackBarColor))
    snackBar.show()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

//for multiple image selection
/*intent.clipData?.let { clipData ->
    for (i in 0 until clipData.itemCount){
        val item = clipData.getItemAt(i).uri
    }
}*/