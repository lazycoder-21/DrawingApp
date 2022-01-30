package lostankit7.droid.drawingapp.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import lostankit7.droid.drawingapp.R
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
    private lateinit var takeImageFromCamera: ActivityResultLauncher<Void>

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

        takeImageFromCamera = takePhotoFromCamera {
            binding.ivDrawingViewBg.setImageBitmap(it)
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

        openGallery.setOnClickListener {
            showImageOptions()
        }

        saveDrawing.setOnClickListener {
            saveDrawing()
        }
        shareDrawing.setOnClickListener {
            saveDrawing(true)
        }
    }

    private fun saveDrawing(share: Boolean = false) {
        updateOrRequestPermission()
        val bitmap = binding.drawingViewContainer.getBitmap()
        val savedSuccessfully = when {
            isPrivate -> savePhotoToInternalStorage(bitmap)
            isWriteGranted -> savePhotoToExternalStorage(bitmap, {
                if (share) shareDrawing(it)
            })
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

    private fun showImageOptions() {
        PopupMenu(this, binding.openGallery).apply {
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.noImage -> {
                        binding.ivDrawingViewBg.setImageURI(null)
                        true
                    }
                    R.id.takePhotoFromCamera -> {
                        takeImageFromCamera.launch(null)
                        true
                    }
                    R.id.chooseFromGallery -> {
                        pickImageFromGallery.launch(intentChooseSingleImage)
                        true
                    }
                    else -> false
                }
            }
            inflate(R.menu.image_picker_options)
            show()
        }
    }
}


