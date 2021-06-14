package com.apurba.vision

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import kotlinx.android.synthetic.main.activity_qrcode_scan.*

class QRCodeScanActivity : AppCompatActivity(), CameraXUtils.ImageCaptureWithAnalyzerListener {
    private lateinit var cameraXUtils : CameraXUtils

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_qrcode_scan)

        createImage();


    }

    private fun createImage(){
        cameraXUtils = CameraXUtils(this)
        cameraXUtils.create(viewFinder)

        if (allPermissionsGranted()) {
            cameraXUtils.startCamera(this, this)
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
//                BitmapUtils.deleteImageFile(this@QRCodeScanActivity, uri.path)

            }
        })
    }






    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults:
        IntArray) {
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                cameraXUtils.startCamera(this, this)
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

    override fun onImageCapturedWithAnalyzer(image: InputImage) {
        Log.d("ARA", " Imaged : got");
    }


}