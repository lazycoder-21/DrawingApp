package lostankit7.droid.drawingapp.ui

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import lostankit7.droid.drawingapp.databinding.ActivityDrawingViewBinding
import lostankit7.droid.drawingapp.databinding.DialogCustomizeBrushBinding
import lostankit7.droid.drawingapp.helper.*
import lostankit7.droid.helper.deviceIndependentValue


class DrawingViewActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDrawingViewBinding
    private val dialogBindingBrush by lazy { DialogCustomizeBrushBinding.inflate(layoutInflater) }
    private val dialogBrush by lazy { showCustomDialog(dialogBindingBrush.root) }
    private lateinit var imagePicker: ActivityResultLauncher<Intent>
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.initClickListener()
        registerCallbacks()

        updateBrushThickness(10)
    }

    private fun registerCallbacks() {
        imagePicker =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode == Activity.RESULT_OK && result.data != null) {
                    result.data?.let { intent ->
                        binding.ivBackground.setImageURI(intent.data)
                    }
                }
            }
        requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    saveDrawing()
                } else {
                    showToast("Oops 😟 you just denied the permission")
                }
            }
    }

    private fun shareDrawing(uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).also {
            it.putExtra(Intent.EXTRA_STREAM, uri)
            it.type = "image/png"
        }
        startActivity(
            Intent.createChooser(
                intent, "Share 💫"
            )
        )

    }

    private fun saveDrawing(share: Boolean = false) {
        val cv = ContentValues().also {
            it.put(MediaStore.MediaColumns.DISPLAY_NAME, "$DRAWING_NAME.jpg")
            it.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                it.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            }
        }
        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return
        if (share)
            shareDrawing(uri)
        else {
            val oos = contentResolver.openOutputStream(uri)
            binding.drawingViewContainer.getBitmap().compress(Bitmap.CompressFormat.JPEG, 100, oos)
            showToast("✍🏻Image Saved To Gallery 🤝")
        }
    }

    private fun View.getBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val viewBg = this.background
        if (viewBg == null) canvas.drawColor(Color.WHITE) else viewBg.draw(canvas)
        this.draw(canvas)
        return bitmap
    }

    private fun customizeBrush() {
        dialogBrush.show()

        dialogBindingBrush.sbBrushSize.getSeekBarValue {
            updateBrushThickness(it)
        }
        dialogBindingBrush.colorPickerView.setColorListener(object : ColorEnvelopeListener {
            override fun onColorSelected(envelope: ColorEnvelope?, fromUser: Boolean) {
                updateBrushColor(Color.parseColor("#${envelope?.hexCode}"))
            }
        })
    }

    /**
     * updates ui if brush color is changed
     */
    private fun updateBrushColor(color: Int) {
        dialogBindingBrush.cvBrushIcon.solidColor(color)
        binding.drawingView.brushColor = color
        binding.brushSelector.setTextColor(color)

    }

    /**
     * updates ui if brush size is changed
     */
    private fun updateBrushThickness(i: Int) {
        val x = resources.deviceIndependentValue(i)
        binding.drawingView.brushThickness = x
        dialogBindingBrush.cvBrushIcon.updateSize(x.toInt())
    }

    private fun ActivityDrawingViewBinding.initClickListener() {
        brushSelector.setOnClickListener { customizeBrush() }
        openGallery.setOnClickListener { imagePicker.launch(intentChooseSingleImage) }
        undoPaint.setOnClickListener { binding.drawingView.undoPaint() }
        redoPaint.setOnClickListener { binding.drawingView.redoPaint() }
        saveDrawing.setOnClickListener {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        shareDrawing.setOnClickListener {
            saveDrawing(true)
        }
    }

    companion object {
        private const val DRAWING_NAME = "MyDrawing"
    }
}


