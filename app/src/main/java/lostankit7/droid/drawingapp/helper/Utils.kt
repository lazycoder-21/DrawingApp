package lostankit7.droid.drawingapp.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import lostankit7.droid.drawingapp.R

fun Activity.showSnackBar(message: String, snackBarColor: Int = R.color.snackBar_error) {
    val snackBar = Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_SHORT)
    snackBar.view.setBackgroundColor(ContextCompat.getColor(this, snackBarColor))
    snackBar.show()
}

fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

fun Activity.shareDrawing(uri: Uri) {
    val intent = Intent(Intent.ACTION_SEND).also {
        it.putExtra(Intent.EXTRA_STREAM, uri)
        it.type = "image/jpeg"
    }
    startActivity(
        Intent.createChooser(
            intent, "Share 💫"
        )
    )

}

//for multiple image selection
/*intent.clipData?.let { clipData ->
    for (i in 0 until clipData.itemCount){
        val item = clipData.getItemAt(i).uri
    }
}*/