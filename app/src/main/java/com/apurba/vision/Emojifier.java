package com.apurba.vision;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.util.List;

import androidx.annotation.NonNull;

public class Emojifier {

    private static final String LOG_TAG = Emojifier.class.getSimpleName();

    public static void detectFaces(Bitmap bitmap){
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions faceDetectorOptions =
                new FaceDetectorOptions.Builder()
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();

        FaceDetector detector = FaceDetection.getClient(faceDetectorOptions);
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        processFaces(faces);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOG_TAG, "##### Face Detection Exception " + e.getMessage());
                                    }
                                });
    }

    private static void processFaces(List<Face> faces){
        if (faces.size() == 0){
            Log.d(LOG_TAG, "No Face Detected");
            return;
        }

        for (Face face : faces) {
            Rect bounds = face.getBoundingBox();
            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees


            // If classification was enabled:
            if (face.getSmilingProbability() != null) {
                float smileProb = face.getSmilingProbability();
                Log.d(LOG_TAG, "Smile Probability : " + smileProb);
            }

            if (face.getRightEyeOpenProbability() != null) {
                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                Log.d(LOG_TAG, "Right Eye Open Probability : " + rightEyeOpenProb);
            }

        }
    }
}
