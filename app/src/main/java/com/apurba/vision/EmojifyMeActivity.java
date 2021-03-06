package com.apurba.vision;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class EmojifyMeActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int REQUEST_PERMISSION_CODE = 12;
    private static final String[] REQUIRED_PERMISSIONS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

    private static final String FILE_PROVIDER_AUTHORITY = "com.apurba.fileprovider";

    private String mTempPhotoPath;

    private Bitmap mResultsBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emojify_me);

        if (!isAllPermissionGranted()){
            requestPermission();
        }else{
            launchCamera();
        }
    }

    private boolean isAllPermissionGranted(){
        for (String requiredPermission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this,
                    requiredPermission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this,
                REQUIRED_PERMISSIONS,
                REQUEST_PERMISSION_CODE);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (isAllPermissionGranted()) {
                launchCamera();
            } else {

                GotoSettingsDialog dialog = new GotoSettingsDialog();
                dialog.show(getSupportFragmentManager(), dialog.getTag());
            }
        }
    }

    /**
     * Creates a temporary image file and captures a picture to store in it.
     */
    private void launchCamera() {

        // Create the capture image intent
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the temporary File where the photo should go
            File photoFile = null;
            try {
                photoFile = BitmapUtils.createTempImageFile(this);
            } catch (IOException ex) {
                // Error occurred while creating the File
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {

                // Get the path of the temporary file
                mTempPhotoPath = photoFile.getAbsolutePath();

                // Get the content URI for the image file
                Uri photoURI = FileProvider.getUriForFile(this,
                        FILE_PROVIDER_AUTHORITY,
                        photoFile);

                // Add the URI so the camera can store the image
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);

                // Launch the camera activity
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // If the image capture activity was called and was successful
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            // Process the image and set it to the TextView
            processAndSetImage();
        } else {

            // Otherwise, delete the temporary image file
            BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        }
    }


    private void processAndSetImage() {
        // Resample the saved image to fit the ImageView
        mResultsBitmap = BitmapUtils.resamplePic(this, mTempPhotoPath);

        // Detect the faces and overlay the appropriate emoji
        Emojifier.detectFacesAndOverlayEmoji(mResultsBitmap, this, new Emojifier.EmojifierListener() {
            @Override
            public void onComplete(Bitmap resultBitmap) {
                mResultsBitmap = resultBitmap;

                if (mResultsBitmap == null){
                    Toast.makeText(EmojifyMeActivity.this, "No Face Detected", Toast.LENGTH_SHORT).show();
                    return;
                }

                ImageView imageView = findViewById(R.id.captured_image);
                imageView.setImageBitmap(mResultsBitmap);
            }

            @Override
            public void onError(Exception e) {
                Toast.makeText(EmojifyMeActivity.this, "Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void saveImage(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
    }

    public void shareImage(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        BitmapUtils.saveImage(this, mResultsBitmap);
        BitmapUtils.shareImage(this, mTempPhotoPath);
    }

    public void clearImage(View view) {
        ImageView imageView = findViewById(R.id.captured_image);
        imageView.setImageResource(0);
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
    }

    public void reLaunchCamera(View view) {
        BitmapUtils.deleteImageFile(this, mTempPhotoPath);
        if (isAllPermissionGranted()){
            launchCamera();
        }else{
            requestPermission();
        }
    }
}