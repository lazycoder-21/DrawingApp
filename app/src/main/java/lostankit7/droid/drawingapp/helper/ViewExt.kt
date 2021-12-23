package lostankit7.droid.drawingapp.helper

import android.app.Activity
import android.app.AlertDialog
import android.view.View
import android.widget.SeekBar

fun View.updateSize(it: Int) {
    layoutParams.width = it
    layoutParams.height = it
    requestLayout()
}

fun SeekBar.getSeekBarValue(seekBarValue: (Int) -> Unit) {
    this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
        override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            seekBarValue(progress)
        }

        override fun onStartTrackingTouch(seekBar: SeekBar?) {

        }

        override fun onStopTrackingTouch(seekBar: SeekBar?) {

        }

    })
}

/**
 * displays a custom dialog and view passes is inflated in dialog
 */
fun Activity.showCustomDialog(view: View): AlertDialog {
    val builder = AlertDialog.Builder(this)
    builder.setView(view)
    return builder.create()
}