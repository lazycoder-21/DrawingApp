package lostankit7.droid.drawingapp.helper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import lostankit7.droid.drawingapp.R

private const val SCHEME = "package"
private const val APP_PKG_NAME_21 = "com.android.settings.ApplicationPkgName"
private const val APP_PKG_NAME_22 = "pkg"
private const val APP_DETAILS_PACKAGE_NAME = "com.android.settings"
private const val APP_DETAILS_CLASS_NAME = "com.android.settings.InstalledAppDetails"

fun Context.showInstalledAppDetails() {
    val intent = Intent()
    val apiLevel = Build.VERSION.SDK_INT
    if (apiLevel >= 9) { // above 2.3
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts(SCHEME, packageName, null)
        intent.data = uri
    } else { // below 2.3
        val appPkgName = if (apiLevel == 8) APP_PKG_NAME_22 else APP_PKG_NAME_21
        intent.action = Intent.ACTION_VIEW
        intent.setClassName(
            APP_DETAILS_PACKAGE_NAME,
            APP_DETAILS_CLASS_NAME
        )
        intent.putExtra(appPkgName, packageName)
    }
    startActivity(intent)
}

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