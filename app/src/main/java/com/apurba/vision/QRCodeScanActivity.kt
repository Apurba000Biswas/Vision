package com.apurba.vision

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_qrcode_scan.*

class QRCodeScanActivity : AppCompatActivity() {
    private lateinit var cameraXUtils : CameraXUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scan)

        cameraXUtils = CameraXUtils(this)
        cameraXUtils.create(viewFinder)

        if (allPermissionsGranted()) {
            cameraXUtils.startCamera(this)
        } else {
            ActivityCompat.requestPermissions(
                this,
                REQUIRED_PERMISSIONS,
                REQUEST_CODE_PERMISSIONS
            )
        }


    }

    fun onTakeSnapshotClicked(view: View) {
        cameraXUtils.takePhoto(object : CameraXUtils.ImageCapturedListener{
            override fun onImageCaptured(uri: Uri) {
                Toast.makeText(this@QRCodeScanActivity, "Captured", Toast.LENGTH_SHORT).show()
            }
        })
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