package lostankit7.droid.drawingapp.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import lostankit7.droid.drawingapp.databinding.ActivityDrawingViewBinding
import lostankit7.droid.drawingapp.databinding.DialogCustomizeBrushBinding
import lostankit7.droid.drawingapp.helper.*
import lostankit7.droid.helper.deviceIndependentValue


class DrawingViewActivity : AppCompatActivity() {

    private var isPrivate = false
    private lateinit var binding: ActivityDrawingViewBinding
    private val dialogBindingBrush by lazy { DialogCustomizeBrushBinding.inflate(layoutInflater) }
    private val dialogBrush by lazy { showCustomDialog(dialogBindingBrush.root) }
    private lateinit var pickImageFromGallery: ActivityResultLauncher<Intent>

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isReadGranted = false
    private var isWriteGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDrawingViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerActivityCallbacks()
        updatePermissionLauncher()
        updateBrushThickness(10)

        binding.initClickListener()

    }

    private fun registerActivityCallbacks() {
        pickImageFromGallery = pickImageFromGallery {
            binding.ivDrawingViewBg.setImageURI(it)
        }

    }

    private fun customizeBrush() {
        dialogBrush.show()

        dialogBindingBrush.sbBrushSize.onProgressChanged {
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
        undoPaint.setOnClickListener { binding.drawingView.undoPaint() }
        redoPaint.setOnClickListener { binding.drawingView.redoPaint() }

        openGallery.setOnClickListener { pickImageFromGallery.launch(intentChooseSingleImage) }

        saveDrawing.setOnClickListener {
            updateOrRequestPermission()
            saveDrawing()
        }
        shareDrawing.setOnClickListener {

        }
    }

    private fun saveDrawing() {
        val bitmap = binding.drawingViewContainer.getBitmap()
        val savedSuccessfully = when {
            isPrivate -> savePhotoToInternalStorage(bitmap)
            isWriteGranted -> savePhotoToExternalStorage(bitmap)
            else -> false
        }
        showToast(if (savedSuccessfully) "Photo saved successfully" else "Failed to save photo")
    }

    private fun updatePermissionLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                isReadGranted =
                    permissions[Manifest.permission.READ_EXTERNAL_STORAGE] ?: isReadGranted
                isWriteGranted =
                    permissions[Manifest.permission.WRITE_EXTERNAL_STORAGE] ?: isWriteGranted
            }
    }

    private fun updateOrRequestPermission() {
        isReadGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        val minSdk29 = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        isWriteGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED || minSdk29

        val permissionsToRequest = mutableListOf<String>()
        if (!isWriteGranted) {
            permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (!isReadGranted) {
            permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionsToRequest.isNotEmpty()) {
            permissionLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}


