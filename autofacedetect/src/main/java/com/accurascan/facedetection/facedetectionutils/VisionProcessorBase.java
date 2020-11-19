package com.accurascan.facedetection.facedetectionutils;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.DisplayMetrics;

import androidx.annotation.GuardedBy;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.accurascan.facedetection.common.BitmapUtils;
import com.accurascan.facedetection.common.FrameMetadata;
import com.accurascan.facedetection.common.GraphicOverlay;
import com.accurascan.facedetection.common.VisionImageProcessor;
import com.accurascan.facedetection.utils.Logger;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.accurascan.facedetection.BuildConfig.DEBUG;

/**
 * Abstract base class for ML Kit frame processors. Subclasses need to implement {@link
 * #onSuccess(Bitmap, Object, FrameMetadata, GraphicOverlay)} to define what they want to with
 * the detection results and {@link #detectInImage(FirebaseVisionImage)} to specify the detector
 * object.
 *
 * @param <T> The type of the detected feature.
 */
public abstract class VisionProcessorBase<T> implements VisionImageProcessor {


    final String TAG = "Processor";

    public final AtomicBoolean isSmother = new AtomicBoolean(false);
    // To keep the latest images and its metadata.
    @GuardedBy("this")
    private ByteBuffer latestImage;

    @GuardedBy("this")
    private FrameMetadata latestImageMetaData;

    // To keep the images and metadata in process.
    @GuardedBy("this")
    private ByteBuffer processingImage;

    @GuardedBy("this")
    private FrameMetadata processingMetaData;

    int width = 0, height = 0;
    DisplayMetrics dm;

    public VisionProcessorBase() {
        dm = Resources.getSystem().getDisplayMetrics();
        width = (int) (dm.widthPixels / BitmapUtils.WIDTH_RATIO);
        height = (int) (width * BitmapUtils.HEIGHT_RATIO);
    }

    @Override
    public void process(ByteBuffer data, FrameMetadata frameMetadata, GraphicOverlay graphicOverlay) throws FirebaseMLException {
//        Logger.e(TAG, "countFrame :-> " + countFrame++);
//        if (!this.isSmother.get()) {
//            processImage(data, frameMetadata, graphicOverlay);
//            Logger.e(TAG, "count :-> " + count++);
            latestImage = data;
            latestImageMetaData = frameMetadata;
            if (processingImage == null && processingMetaData == null) {
                processLatestImage(graphicOverlay);
            }
//        }
    }

    @Override
    public void process(Bitmap bitmap, GraphicOverlay graphicOverlay) {
        if (!this.isSmother.get())
            detectInVisionImage(null /* bitmap */, FirebaseVisionImage.fromBitmap(bitmap), null,
                    graphicOverlay);
    }

    private synchronized void processLatestImage(final GraphicOverlay graphicOverlay) {
        processingImage = latestImage;
        processingMetaData = latestImageMetaData;
        latestImage = null;
        latestImageMetaData = null;
        if (processingImage != null && processingMetaData != null) {
            processImage(processingImage, processingMetaData, graphicOverlay);
        }
    }

    private void processImage(
            ByteBuffer data, final FrameMetadata frameMetadata,
            final GraphicOverlay graphicOverlay) {

        // To create the FirebaseVisionImage

        Bitmap bitmap = BitmapUtils.getBitmap(data, frameMetadata);
        if (bitmap == null) {
            return;
        }
        int croppedWidth = width;
        int croppedHeight = height;
        Point centerOfCanvas = new Point(dm.widthPixels / 2, dm.heightPixels / 2);
        int left = centerOfCanvas.x - (croppedWidth / 2);
        int top = centerOfCanvas.y - (croppedHeight / 2);
        int right = centerOfCanvas.x + (croppedWidth / 2);
        int bottom = centerOfCanvas.y + (croppedHeight / 2);
        Rect frameRect = new Rect(left, top, right, bottom);
        float widthScaleFactor = (float) dm.widthPixels / (float) bitmap.getWidth();
        frameRect.left = (int) (frameRect.left / widthScaleFactor);
        frameRect.right = (int) (frameRect.right / widthScaleFactor);
        float heightOffset = ((frameRect.right-frameRect.left) * BitmapUtils.HEIGHT_RATIO) / 2;
        frameRect.top = (int) ((bitmap.getHeight() * 0.5f) - heightOffset);
        frameRect.bottom = (int) ((bitmap.getHeight() * 0.5f) + heightOffset);
        Rect finalrect = new Rect((int) (frameRect.left), (int) (frameRect.top), (int) (frameRect.right), (int) (frameRect.bottom));

        FirebaseVisionImage firebaseVisionImage = null;
        try {
            int l1 = Math.max(finalrect.left, 0);
            int t1 = Math.max(finalrect.top, 0);
            int w = Math.min(finalrect.width(), bitmap.getWidth());
            int h = Math.min(finalrect.height(), bitmap.getHeight());
            frameMetadata.setRect(new Rect(l1,t1,l1+w,t1+h));
        } catch (Exception e) {
            Logger.e(TAG, "Process " + e.getMessage());
            if (DEBUG) {
                e.printStackTrace();
            }
        }
//        firebaseVisionImage = FirebaseVisionImage.fromBitmap(bitmap);
        FirebaseVisionImageMetadata metadata =
                new FirebaseVisionImageMetadata.Builder()
                        .setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
                        .setWidth(frameMetadata.getWidth())
                        .setHeight(frameMetadata.getHeight())
                        .setRotation(frameMetadata.getRotation())
                        .build();

        firebaseVisionImage = FirebaseVisionImage.fromByteBuffer(data, metadata);

        detectInVisionImage(
                bitmap, firebaseVisionImage, frameMetadata,
                graphicOverlay);

    }

    private void detectInVisionImage(
            final Bitmap originalCameraImage, FirebaseVisionImage image,
            final FrameMetadata metadata, final GraphicOverlay graphicOverlay) {
        detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<T>() {
                    @Override
                    public void onSuccess(T results) {
                        VisionProcessorBase.this.onSuccess(originalCameraImage, results,
                                metadata,
                                graphicOverlay);
                        processLatestImage(graphicOverlay);
                        VisionProcessorBase.this.isSmother.set(false);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        VisionProcessorBase.this.isSmother.set(false);
                        VisionProcessorBase.this.onFailure(e);
                    }
                });
        this.isSmother.set(true);
    }

    @Override
    public void stop() {
    }

    protected abstract Task<T> detectInImage(FirebaseVisionImage image);

    /**
     * Callback that executes with a successful detection result.
     *
     * @param originalCameraImage hold the original image from camera, used to draw the background
     *                            image.
     */
    protected abstract void onSuccess(
            @Nullable Bitmap originalCameraImage,
            @NonNull T results,
            @NonNull FrameMetadata frameMetadata,
            @NonNull GraphicOverlay graphicOverlay);

    protected abstract void onFailure(@NonNull Exception e);
}
