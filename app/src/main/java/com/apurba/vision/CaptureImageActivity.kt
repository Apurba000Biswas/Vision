package com.apurba.vision

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_image_capture.*

class CaptureImageActivity : AppCompatActivity() {

    private lateinit var previewUrl: String;
    private lateinit var cameraXUtils : CameraXUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_capture)

        cameraXUtils = CameraXUtils(this)
        cameraXUtils.create(viewFinder)

        // Request camera permissions
        if (allPermissionsGranted()) {
            cameraXUtils.startCamera(this)
        } else {
            ActivityCompat.requestPermissions(
                this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
        }

        cameraXUtils.getPreviewLastImageCapturedUri()?.let { showImagePreview(it) };

        camera_capture_button.setOnClickListener { cameraXUtils.takePhoto(object : CameraXUtils.ImageCapturedListener{
            override fun onImageCaptured(uri: Uri) {
                showImagePreview(uri)
            }
        }) }

        iv_image_preview.setOnClickListener {
            val imageViewIntent = Intent(this, ImageViewActivity::class.java)
            val bundle: Bundle = ActivityOptions
                .makeSceneTransitionAnimation(
                    this, it, it.transitionName
                )
                .toBundle()
            imageViewIntent.putExtra("uri", previewUrl)
            startActivity(imageViewIntent, bundle)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraXUtils.startCamera(this)
            } else {
                val dialog = GotoSettingsDialog()
                dialog.show(supportFragmentManager, dialog.tag)
                finish()
            }
        }
    }

    private fun showImagePreview(uri: Uri){
        previewUrl = uri.toString()
        Picasso.get()
            .load(uri)
            .placeholder(R.mipmap.ic_launcher_round)
            .into(iv_image_preview)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }


    override fun onDestroy() {
        super.onDestroy()
        cameraXUtils.cameraExecutor.shutdown()
    }

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA)
    }

}