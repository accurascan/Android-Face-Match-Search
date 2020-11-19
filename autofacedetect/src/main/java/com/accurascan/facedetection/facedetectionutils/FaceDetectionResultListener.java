package com.accurascan.facedetection.facedetectionutils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.accurascan.facedetection.common.FrameMetadata;
import com.accurascan.facedetection.common.GraphicOverlay;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

public interface FaceDetectionResultListener {
    void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull FirebaseVisionFace faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    void onFailure(@NonNull Exception e);

    void onFeedBackMessage(@NonNull String s);
}
