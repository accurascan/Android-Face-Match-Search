package com.accurascan.facedetection.facedetectionutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.accurascan.facedetection.CameraScreenCustomization;
import com.accurascan.facedetection.common.BitmapUtils;
import com.accurascan.facedetection.common.FrameMetadata;
import com.accurascan.facedetection.common.GraphicOverlay;
import com.accurascan.facedetection.utils.Logger;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;

import java.io.IOException;
import java.util.List;

public class FaceDetectionProcessor extends VisionProcessorBase<List<FirebaseVisionFace>> {

    static {
        System.loadLibrary("accurafacedetect");
    }

    public static final String ACCURA_FEEDBACK_MOVE_PHONE_CENTER = "Move Phone Center";
    public static final String ACCURA_FEEDBACK_OPEN_EYES = "Keep Your Eyes Open";
    public static final String ACCURA_FEEDBACK_MOVE_PHONE_AWAY = "Move Phone Away";
    public static final String ACCURA_FEEDBACK_MOVE_PHONE_CLOSER = "Move Phone Closer";
    public static final String ACCURA_FEEDBACK_FRAME_YOUR_FACE = "Frame Your Face";
    public static final String ACCURA_FEEDBACK_MULTIPLE_FACES = "Multiple face detected";
    public static final String ACCURA_FEEDBACK_HEAD_STEADY = "Keep Your Head Straight";
    public static final String ACCURA_FEEDBACK_LOW_LIGHT = "Low light detected";
    public static final String ACCURA_FEEDBACK_BLUR = "Blur detected over face";
    public static final String ACCURA_FEEDBACK_GLARE = "Glare detected over face";

    public static final String ACCURA_FEEDBACK_CODE_DARK_FACE = "-1";
    public static final String ACCURA_FEEDBACK_CODE_BLUR_FACE = "-2";
    public static final String ACCURA_FEEDBACK_CODE_GLARE = "-3";

    private final FirebaseVisionFaceDetector detector;

    FaceDetectionResultListener faceDetectionResultListener;
    private CameraScreenCustomization cameraScreenCustomization;

    private static final long CLICK_TIME_INTERVAL = 10;

    private long mLastClickTime;
    private boolean takePicture = true;

    public FaceDetectionProcessor() {
        FirebaseVisionFaceDetectorOptions options =
                new FirebaseVisionFaceDetectorOptions.Builder()
                        .setPerformanceMode(FirebaseVisionFaceDetectorOptions.ACCURATE)
                        .setLandmarkMode(FirebaseVisionFaceDetectorOptions.ALL_LANDMARKS)
                        .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                        .build();

        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

        this.mLastClickTime = System.currentTimeMillis();
        takePicture = true;
    }

    public FaceDetectionProcessor(FirebaseVisionFaceDetector detector) {
        this.mLastClickTime = System.currentTimeMillis();
        this.detector = detector;
    }

    public FaceDetectionResultListener getFaceDetectionResultListener() {
        return faceDetectionResultListener;
    }

    public void setFaceDetectionResultListener(FaceDetectionResultListener faceDetectionResultListener) {
        this.faceDetectionResultListener = faceDetectionResultListener;
    }

    public void setCustomization(CameraScreenCustomization cameraScreenCustomization) {
        this.cameraScreenCustomization = cameraScreenCustomization;
    }

    public boolean isTakePicture() {
        return takePicture;
    }

    public void setTakePicture(boolean takePicture) {
        this.takePicture = takePicture;
    }

    @Override
    public void stop() {
        try {
            detector.close();
        } catch (IOException e) {
            Logger.e(TAG, "Exception thrown while trying to close Face Detector: " + e);
        }
    }

    @Override
    protected Task<List<FirebaseVisionFace>> detectInImage(FirebaseVisionImage image) {
        return detector.detectInImage(image);
    }

    @Override
    protected void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull List<FirebaseVisionFace> faces,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay) {

        if (originalCameraImage == null)
            return;
        int index = -1;
        int indexW = 0;
        int indexH = 0;
        int currentPosition = 0;
        Logger.e(TAG, "onSuccess" + faces.size());
        if (faces.size()>1) {
            originalCameraImage.recycle();
            faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MULTIPLE_FACES);
            return;
        }
        for (FirebaseVisionFace visionFace : faces) {
            Rect bounds = visionFace.getBoundingBox();
            if (bounds.width() > indexW || bounds.height() > indexH) {
                indexW = bounds.width();
                indexH = bounds.height();
                index = currentPosition;
            }
            currentPosition++;
        }

        if (index > -1) {
            FirebaseVisionFace face = faces.get(index);
            long now = System.currentTimeMillis();
            Rect rect = face.getBoundingBox();
            if (isTakePicture() && faceDetectionResultListener != null && now - mLastClickTime > CLICK_TIME_INTERVAL) {
//                Logger.e(TAG, "curre " + rect.toString() + "(" + rect.width() + "," + rect.height() + ")" + "(" + frameMetadata.getRect().width() + "," + frameMetadata.getRect().height() + ")");

                // oval Rectangle
                Rect ovalRect = frameMetadata.getRect();

                //<editor-fold desc="Outer Rectangle">
                float wX = ovalRect.width() * 0.15f;
                float wY = ovalRect.height() * 0.15f;
                int left = (int) (ovalRect.left - wX);
                int top = (int) (ovalRect.top - wY);
                int right = (int) (ovalRect.right + wX);
                int bottom = (int) (ovalRect.bottom + wY);
                Rect extendOval = new Rect(left, top, right, bottom);
                //</editor-fold>

                //<editor-fold desc="Inner Rectangle">
                wX = ovalRect.width() * 0.20f;
                wY = ovalRect.height() * 0.25f;
                left = (int) (ovalRect.left + wX);
                top = (int) (ovalRect.top + wY);
                right = (int) (ovalRect.right - wX);
                bottom = (int) (ovalRect.bottom - wY);
                Rect insetOval = new Rect(left, top, right, bottom);
                //</editor-fold>

                if (-10.0 >= face.getHeadEulerAngleZ()  || face.getHeadEulerAngleZ() >= 10) {
                    // head Message
                    Logger.e(TAG, "He - " + face.getHeadEulerAngleZ() + " " + (-10.0 >= face.getHeadEulerAngleZ()) + (face.getHeadEulerAngleZ() >= 10));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_HEAD_STEADY);
                    return;
                }
                if (-10.0 >= face.getHeadEulerAngleY() || face.getHeadEulerAngleY() >= 10) {
                    // center Message
                    Logger.e(TAG, "Ce - " + face.getHeadEulerAngleY() + " " + (-10.0 >= face.getHeadEulerAngleY()) + (face.getHeadEulerAngleY() >= 10));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_CENTER);
                    return;
                }

                if (rect.left < extendOval.left || rect.top < extendOval.top || rect.right > extendOval.right || rect.bottom > extendOval.bottom) {
                    wX = ovalRect.width() * 0f;
                    wY = ovalRect.height() * 0f;
                    left = (int) (ovalRect.left + wX);
                    top = (int) (ovalRect.top + wY);
                    right = (int) (ovalRect.right - wX);
                    bottom = (int) (ovalRect.bottom - wY);
                    int centerX = left + ((right - left)/3);
                    int centerY = top + ((bottom - top)/3);
                    if ((rect.left < left && rect.right > centerX) || (rect.left > centerX && rect.right > right) ||
                            (rect.top < top && rect.bottom < centerY) || (rect.top > centerY && rect.bottom > bottom)) {
                        // center message
                        Logger.e(TAG, "A - " + ((rect.left < left && rect.right > centerX)?1:0) +
                                ((rect.left > centerX && rect.right > right)?1:0) + ((rect.top < top && rect.bottom < centerY)?1:0) +
                                ((rect.top > centerY && rect.bottom > bottom)?1:0));
                        faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_CENTER);
                        return;
                    }
                    // away message
                    Logger.e(TAG, "A| - " + (rect.left < extendOval.left?1:0) + (rect.top < extendOval.top?1:0) + (rect.right > extendOval.right?1:0) + (rect.bottom > extendOval.bottom?1:0));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_AWAY);
                    return;
                }
                if (rect.width() < (ovalRect.width() * 0.40f) || rect.height() < (ovalRect.height() * 0.40f)/*rect.left > insetOval.left || rect.top > insetOval.top || rect.right < insetOval.right || rect.bottom < insetOval.bottom*/) {
                    // closer message
                    Logger.e(TAG, "C - " + (rect.width() < (ovalRect.width() * 0.30f)?1:0) + (rect.height() < (ovalRect.height() * 0.30f)?1:0));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_CLOSER);
                    return;
                }
                if (face.getLeftEyeOpenProbability() < 0.8 || face.getRightEyeOpenProbability() < 0.8) {
                    // keep Eyes open message
                    Logger.e(TAG, "O - " + (face.getLeftEyeOpenProbability() < 0.8?1:0) + (face.getRightEyeOpenProbability() < 0.8?1:0));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_OPEN_EYES);
                    return;
                }
                mLastClickTime = now;
//                if (true/*ffx - point <= fx && fx <= ffx + point && ffy - point <= fy && fy <= ffy + point*/) {
                if (rect.left >= extendOval.left && rect.top >= extendOval.top && rect.right <= extendOval.right && rect.bottom <= extendOval.bottom
                    /*&& rect.left <= insetOval.left && rect.top <= insetOval.top && rect.right >= insetOval.right && rect.bottom >= insetOval.bottom*/) {
                    Logger.e(TAG, "O   ");// over
                    try {
                        Bitmap bitmap = Bitmap.createBitmap(originalCameraImage, ovalRect.left, ovalRect.top, ovalRect.width(), ovalRect.height());

                        if (bitmap != null) {
                            //                        float x = frameMetadata.getBitmap().getWidth();
                            //                        float y = frameMetadata.getBitmap().getHeight();
                            //                        float xOffset = frameMetadata.getBitmap().getWidth() / 2.0f;
                            //                        float yOffset = frameMetadata.getBitmap().getHeight() / 2.0f;
                            //                        int left = (int) (x - xOffset);
                            //                        int top = (int) (y - yOffset);
                            //                        int right = (int) (x + xOffset);
                            //                        int bottom = (int) (y + yOffset);
                            //                        Bitmap bitmap = Bitmap.createBitmap(frameMetadata.getBitmap(), (int) fleft, (int) ftop, (int) (fright - fleft), (int) (fbottom - ftop));
                            int ret = doCheckFace(bitmap, cameraScreenCustomization.lowLightPercentage, cameraScreenCustomization.blurPercentage, cameraScreenCustomization.glareMinPercentage, cameraScreenCustomization.glareMaxPercentage);
                            Logger.e(TAG, "B " + ret);// blur
                            bitmap.recycle();
                            if (ret > 0) {
                                setTakePicture(false);
                                if (extendOval.left < 0) extendOval.left = 0;
                                if (extendOval.top < 0) extendOval.top = 0;
                                if (extendOval.right > originalCameraImage.getWidth()) extendOval.right = originalCameraImage.getWidth();
                                if (extendOval.bottom > originalCameraImage.getWidth()) extendOval.bottom = originalCameraImage.getHeight();
                                frameMetadata.setRect(extendOval);
                                faceDetectionResultListener.onSuccess(originalCameraImage, face, frameMetadata, graphicOverlay);
//                                originalCameraImage.recycle();
                            } else {
                                faceDetectionResultListener.onFeedBackMessage("" + ret + "");
                                if (!originalCameraImage.isRecycled())
                                    originalCameraImage.recycle();
                            }
                        }
                    } catch (Exception e) {
                        Logger.e(TAG, "CA - " + e.getMessage());
                        if (!originalCameraImage.isRecycled())
                            originalCameraImage.recycle();
                        e.printStackTrace();
                    }
                }
                else if (!originalCameraImage.isRecycled())
                    originalCameraImage.recycle();
            }
            else if (!originalCameraImage.isRecycled())
                originalCameraImage.recycle();
        } else if (!originalCameraImage.isRecycled()){
            originalCameraImage.recycle();
            faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_FRAME_YOUR_FACE);
        }
    }

    @Override
    protected void onFailure(@NonNull Exception e) {

        if (faceDetectionResultListener != null)
            faceDetectionResultListener.onFailure(e);
    }

    /**
     * To check face validation all values are between 0-100
     * if remove low light validation then set light tolerance to -1
     * if remove Blur validation then set light faceBlurPercentage to -1
     * if remove Glare validation then set light minGlarePercentage & maxGlarePercentage to -1
     *
     * @param bitmap
     * @param lightTolerance
     * @param faceBlurPercentage
     * @param minGlarePercentage
     * @param maxGlarePercentage
     * @return
     */
    public native int doCheckFace(Bitmap bitmap, int lightTolerance, int faceBlurPercentage, int minGlarePercentage, int maxGlarePercentage);
    public native int doCheckData(byte[] yuvdata, int width, int height);
    private native String doLightCheck(Bitmap bitmap, int i);
}
