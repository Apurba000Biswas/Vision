package com.apurba.vision;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
    private static final float EMOJI_SCALE_FACTOR = .9f;
    private static final double SMILING_PROB_THRESHOLD = .15;
    private static final double EYE_OPEN_PROB_THRESHOLD = .5;



    public static void detectFacesAndOverlayEmoji(final Bitmap bitmap
            , final Context context
            , final EmojifierListener listener){
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
                                        Bitmap resultBitmap = processFaces(faces, context, bitmap);
                                        listener.onComplete(resultBitmap);
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d(LOG_TAG, "##### Face Detection Exception " + e.getMessage());
                                        listener.onError(e);
                                    }
                                });
    }

    private static Bitmap processFaces(List<Face> faces, Context context, Bitmap originalBitmap){
        if (faces.size() == 0){
            Log.d(LOG_TAG, "No Face Detected");
            return null;
        }

        Bitmap emojiBitmap;
        Bitmap resultBitmap = originalBitmap;
        for (Face face : faces) {
            Resources res = context.getResources();
            switch ( whichEmoji(face)){
                case FROWN:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.frown);
                    break;
                case SMILE:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.smile);
                    break;
                case LEFT_WINK:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.leftwink);
                    break;
                case RIGHT_WINK:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.rightwink);
                    break;
                case LEFT_WINK_FROWN:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.leftwinkfrown);
                    break;
                case RIGHT_WINK_FROWN:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.rightwinkfrown);
                    break;
                case CLOSED_EYE_FROWN:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.closed_frown);
                    break;
                case CLOSED_EYE_SMILE:
                    emojiBitmap = BitmapFactory.decodeResource(res, R.drawable.closed_smile);
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + whichEmoji(face));
            }
            resultBitmap = addBitmapToFace(resultBitmap, emojiBitmap, face);
        }
        return resultBitmap;
    }

    private static Emoji whichEmoji(Face face){
        boolean smiling = face.getSmilingProbability() > SMILING_PROB_THRESHOLD;
        boolean leftEyeClosed = face.getLeftEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;
        boolean rightEyeClosed = face.getRightEyeOpenProbability() < EYE_OPEN_PROB_THRESHOLD;


        // Determine and log the appropriate emoji
        Emoji emoji;
        if(smiling) {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK;
            } else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_SMILE;
            } else {
                emoji = Emoji.SMILE;
            }
        } else {
            if (leftEyeClosed && !rightEyeClosed) {
                emoji = Emoji.LEFT_WINK_FROWN;
            }  else if(rightEyeClosed && !leftEyeClosed){
                emoji = Emoji.RIGHT_WINK_FROWN;
            } else if (leftEyeClosed){
                emoji = Emoji.CLOSED_EYE_FROWN;
            } else {
                emoji = Emoji.FROWN;
            }
        }
        
        // Log the chosen Emoji
        Log.d(LOG_TAG ,"Which Emoji ? : " + emoji.name());

        // return the chosen Emoji
        return emoji;

    }

    /**
     * Combines the original picture with the emoji bitmaps
     *
     * @param backgroundBitmap The original picture
     * @param emojiBitmap      The chosen emoji
     * @param face             The detected face
     * @return The final bitmap, including the emojis over the faces
     */
    private static Bitmap addBitmapToFace(Bitmap backgroundBitmap, Bitmap emojiBitmap, Face face) {

        // Initialize the results bitmap to be a mutable copy of the original image
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(),
                backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        // Scale the emoji so it looks better on the face
        float scaleFactor = EMOJI_SCALE_FACTOR;

        Rect bounds = face.getBoundingBox();

        // Determine the size of the emoji to match the width of the face and preserve aspect ratio
        int newEmojiWidth = (int) (bounds.width() * scaleFactor);
        int newEmojiHeight = (int) (emojiBitmap.getHeight() *
                newEmojiWidth / emojiBitmap.getWidth() * scaleFactor);


        // Scale the emoji
        emojiBitmap = Bitmap.createScaledBitmap(emojiBitmap, newEmojiWidth, newEmojiHeight, false);

        // Determine the emoji position so it best lines up with the face
        float emojiPositionX = bounds.left;
                //(bounds.top + bounds.width() / 2) - emojiBitmap.getWidth() / 2;
        float emojiPositionY = bounds.top;
                //(bounds.top + bounds.height() / 2) - emojiBitmap.getHeight() / 3;

        // Create the canvas and draw the bitmaps to it
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, 0, 0, null);
        canvas.drawBitmap(emojiBitmap, emojiPositionX, emojiPositionY, null);

        return resultBitmap;
    }

    public interface EmojifierListener{
        void onComplete(Bitmap resultBitmap);
        void onError(Exception e);
    }
}
