package com.accurascan.facedetection.facedetectionutils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    private final FirebaseVisionFaceDetector detector;

    FaceDetectionResultListener faceDetectionResultListener;

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
                float wX = ovalRect.width() * 0.1f;
                float wY = ovalRect.height() * 0.1f;
                int left = (int) (ovalRect.left - wX);
                int top = (int) (ovalRect.top - wY);
                int right = (int) (ovalRect.right + wX);
                int bottom = (int) (ovalRect.bottom + wY);
                Rect extendOval = new Rect(left, top, right, bottom);
                //</editor-fold>

                //<editor-fold desc="Inner Rectangle">
                wX = ovalRect.width() * 0.17f;
                wY = ovalRect.height() * 0.2f;
                left = (int) (ovalRect.left + wX);
                top = (int) (ovalRect.top + wY);
                right = (int) (ovalRect.right - wX);
                bottom = (int) (ovalRect.bottom - wY);
                Rect insetOval = new Rect(left, top, right, bottom);
                //</editor-fold>
//                Logger.e(TAG, "extend " + extendOval.toString());
//                Logger.e(TAG, "oval " + ovalRect.toString());
//                Logger.e(TAG, "inset " + insetOval.toString());


//                float fx = rect.exactCenterX();
//                float fy = rect.exactCenterY();
//                float ffx = frameMetadata.getBitmap().getWidth() / 2f;
//                float ffy = frameMetadata.getBitmap().getHeight() / 2f;
//                int point = 200;
//                Logger.e(TAG, "(" + fx + "," + fy + ")(" + ffx + "," + ffy + ")");
//                if (rect.left >= frameMetadata.getRect().left - 80 && rect.top >= frameMetadata.getRect().top - 80
//                        && rect.right <= frameMetadata.getRect().right +100
//                        && rect.bottom <= frameMetadata.getRect().bottom +100) {
//                if ((rect.left < extendOval.left || rect.right > extendOval.right) && (rect.bottom > extendOval.bottom || rect.top < extendOval.top) ) {
//                    faceDetectionResultListener.onUserInteraction("Move Phone Away");
//                    return;
//                }
//                if ((rect.left > insetOval.left || rect.right < insetOval.right) && (rect.bottom < insetOval.bottom || rect.top > insetOval.top)) {
//                    faceDetectionResultListener.onUserInteraction("Move Phone Closer");
//                    return;
//                }
                if (-10.0 >= face.getHeadEulerAngleY() || face.getHeadEulerAngleY() >= 10) {
                    // center Message
                    Logger.e(TAG, "Ce - " + face.getHeadEulerAngleY() + " " + (-10.0 >= face.getHeadEulerAngleY()) + (face.getHeadEulerAngleY() >= 10));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_CENTER);
                    return;
                }
                if (face.getLeftEyeOpenProbability() < 0.8 || face.getRightEyeOpenProbability() < 0.8) {
                    // keep Eyes open message
                    Logger.e(TAG, "O - " + (face.getLeftEyeOpenProbability() < 0.8?1:0) + (face.getRightEyeOpenProbability() < 0.8?1:0));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_OPEN_EYES);
                    return;
                }
                if (rect.left < extendOval.left || rect.top < extendOval.top || rect.right > extendOval.right || rect.bottom > extendOval.bottom) {
                    // away message
                    Logger.e(TAG, "A - " + (rect.left < extendOval.left?1:0) + (rect.top < extendOval.top?1:0) + (rect.right > extendOval.right?1:0) + (rect.bottom > extendOval.bottom?1:0));
                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_AWAY);
                    return;
                }
//                if (rect.left > insetOval.left || rect.top > insetOval.top || rect.right < insetOval.right || rect.bottom < insetOval.bottom) {
//                    // closer message
//                    Logger.e(TAG, "C - " + (rect.left > insetOval.left?1:0) + (rect.top > insetOval.top?1:0) + (rect.right < insetOval.right?1:0) + (rect.bottom < insetOval.bottom?1:0));
//                    faceDetectionResultListener.onFeedBackMessage(ACCURA_FEEDBACK_MOVE_PHONE_CLOSER);
//                    return;
//                }
                mLastClickTime = now;
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
                        int ret = doCheckFace(bitmap, 39, 80, 6, 98);
                        Logger.e(TAG, "B " + ret);// blur
                        bitmap.recycle();
                        if (ret > 0) {
//                                //<editor-fold desc="Draw rect on a bitmap to check validation">
//                                Canvas canvas = new Canvas(originalCameraImage);
//                                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//                                paint.setColor(Color.RED);
//                                paint.setStyle(Paint.Style.STROKE);
//                                paint.setStrokeWidth(3.0f);
//                                canvas.drawRect(face.getBoundingBox(), paint);
//                                paint.setColor(Color.BLUE);
//                                canvas.drawRect(extendOval, paint);
//                                paint.setColor(Color.BLUE);
//                                canvas.drawRect(insetOval, paint);
//                                paint.setColor(Color.YELLOW);
//                                canvas.drawRect(frameMetadata.getRect(), paint);
//                                RectF rectF = new RectF(ovalRect.left, ovalRect.top, ovalRect.right, ovalRect.bottom);
//                                paint.setColor(Color.WHITE);
//                                canvas.drawOval(rectF, paint);
//                                //</editor-fold>
                            setTakePicture(false);
//                                Bitmap bitmap1 = BitmapUtils.centerCrop(originalCameraImage, originalCameraImage.getWidth(), originalCameraImage.getWidth());
                            frameMetadata.setRect(extendOval);
                            faceDetectionResultListener.onSuccess(originalCameraImage, face, frameMetadata, graphicOverlay);
//                                originalCameraImage.recycle();
                        } else if (!originalCameraImage.isRecycled())
                            originalCameraImage.recycle();
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
     * @param bitmap
     * @return
     */
    public native int doCheckFace(Bitmap bitmap, int lightTolerance, int faceBlurPercentage, int minPercentage, int maxPercentage);
    public native int doCheckData(byte[] yuvdata, int width, int height);
    private native String doLightCheck(Bitmap bitmap, int i);
}
