package com.accurascan.facedetection;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.accurascan.facedetection.common.BitmapUtils;
import com.accurascan.facedetection.common.CameraSource;
import com.accurascan.facedetection.common.CameraSourcePreview;
import com.accurascan.facedetection.common.FrameMetadata;
import com.accurascan.facedetection.common.GraphicOverlay;
import com.accurascan.facedetection.facedetectionutils.CircleOverlayView;
import com.accurascan.facedetection.facedetectionutils.FaceDetectionProcessor;
import com.accurascan.facedetection.facedetectionutils.FaceDetectionResultListener;
import com.accurascan.facedetection.utils.Logger;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public final class SelfieCameraActivity extends AppCompatActivity
        implements OnRequestPermissionsResultCallback {
    private static final String TAG = "SelfieCamera";
    private static final int PERMISSION_REQUESTS = 1;

    private CameraSource cameraSource = null;
    private GraphicOverlay fireFaceOverlay = null;
    private CameraSourcePreview cameraPreview;
    private FaceDetectionProcessor processor;
    FaceDetectionResultListener faceDetectionResultListener = null;
    private Uri myUri;
    private TextView tv_status;
    private RelativeLayout centerFrameLayout, feedbackContainer;
    CameraScreenCustomization cameraScreenCustomization;

    public static Intent SelfieCameraIntent(Activity activity, CameraScreenCustomization cameraScreenCustomization, Uri uriForFile) {
        Intent intent = new Intent(activity, SelfieCameraActivity.class);
        if (cameraScreenCustomization != null) {
            intent.putExtra("accurascan.autocapture.customization", cameraScreenCustomization);
        }
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile.toString());
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (getWindow() != null) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selfie_camera);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Bundle extras = getIntent().getExtras();
        if (extras != null && extras.containsKey(MediaStore.EXTRA_OUTPUT)) {
            String s = extras.getString(MediaStore.EXTRA_OUTPUT);
            if (s != null) {
                myUri = Uri.parse(s);
            }
        }

        if (getIntent().hasExtra("accurascan.autocapture.customization"))
            cameraScreenCustomization = getIntent().getParcelableExtra("accurascan.autocapture.customization");
        if (cameraScreenCustomization == null) {
            cameraScreenCustomization = new CameraScreenCustomization();

            cameraScreenCustomization.backGroundColor = 0xFFC4C4C5;
            cameraScreenCustomization.closeIconColor = 0xFF000000;

            cameraScreenCustomization.feedbackBackGroundColor = Color.TRANSPARENT;
            cameraScreenCustomization.feedbackTextColor = Color.BLACK;
            cameraScreenCustomization.feedbackTextSize = 18;

            cameraScreenCustomization.feedBackframeMessage = "Frame Your Face";
            cameraScreenCustomization.feedBackAwayMessage = "Move Phone Away";
            cameraScreenCustomization.feedBackOpenEyesMessage = "Keep Your Eyes Open";
            cameraScreenCustomization.feedBackCloserMessage = "Move Phone Closer";
            cameraScreenCustomization.feedBackCenterMessage = "Move Phone Center";

        }

        DisplayMetrics dm = getResources().getDisplayMetrics();
        int width = (int) (dm.widthPixels / BitmapUtils.WIDTH_RATIO);
        int height = (int) (width * BitmapUtils.HEIGHT_RATIO);
        centerFrameLayout = findViewById(R.id.centerFrameLayout);
        cameraPreview = findViewById(R.id.cameraPreview);
        tv_status = findViewById(R.id.tv_feedBack);
        feedbackContainer = findViewById(R.id.feedbackContainer);
        ImageView oval_layout = findViewById(R.id.oval_layout);
        ImageView imClose = findViewById(R.id.im_close);

        imClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        oval_layout.getLayoutParams().width = width;
        oval_layout.getLayoutParams().height = height;
        oval_layout.setBackground(getResources().getDrawable(R.drawable.camera_overlay_frames));

        imClose.setColorFilter(cameraScreenCustomization.closeIconColor, android.graphics.PorterDuff.Mode.SRC_IN);

        tv_status.setText(TextUtils.isEmpty(cameraScreenCustomization.feedBackframeMessage) ? FaceDetectionProcessor.ACCURA_FEEDBACK_FRAME_YOUR_FACE : cameraScreenCustomization.feedBackframeMessage);
        tv_status.setTextColor(cameraScreenCustomization.feedbackTextColor);
        tv_status.setTextSize(cameraScreenCustomization.feedbackTextSize > 0 ? cameraScreenCustomization.feedbackTextSize : 18);

        FeedBackShadow sp = new FeedBackShadow()
                .setShadowColor(0x77000000)
                .setShadowDy(dip2px(this, 0.5f))
                .setShadowRadius(dip2px(this, 3))
                .setShadowSide(FeedBackShadow.ALL);
        FeedBackView sd = new FeedBackView(sp, cameraScreenCustomization.feedbackBackGroundColor, 8, 8);
        ViewCompat.setBackground(feedbackContainer, sd);
        feedbackContainer.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        CircleOverlayView circleOverlayView = new CircleOverlayView(this);
        circleOverlayView.setColor(cameraScreenCustomization.backGroundColor);
        circleOverlayView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        centerFrameLayout.removeAllViews();
        centerFrameLayout.addView(circleOverlayView);

        if (allPermissionsGranted()) {
            createCameraSource();
        } else {
            getRuntimePermissions();
        }
    }

    private void createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (cameraSource == null) {
            cameraSource = new CameraSource(this, fireFaceOverlay);
            cameraSource.setFacing(CameraSource.CAMERA_FACING_FRONT);
        }

        try {
            processor = new FaceDetectionProcessor();
            processor.setFaceDetectionResultListener(getFaceDetectionListener());
            cameraSource.setMachineLearningFrameProcessor(processor);
        } catch (Exception e) {
            Toast.makeText(
                    getApplicationContext(),
                    "Can not create image processor: " + e.getMessage(),
                    Toast.LENGTH_LONG)
                    .show();
        }

    }

    String status = "";

    private FaceDetectionResultListener getFaceDetectionListener() {
        if (faceDetectionResultListener == null)
            faceDetectionResultListener = new FaceDetectionResultListener() {
                @Override
                public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull FirebaseVisionFace face, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {

                    Bitmap bitmap1 = BitmapUtils.centerCrop(originalCameraImage, originalCameraImage.getWidth(), originalCameraImage.getWidth());

                    if (bitmap1 != null && !bitmap1.isRecycled()) {

                        Bitmap bmCard = null;
                        try {
                            Rect rect = frameMetadata.getRect();
                            bmCard = Bitmap.createBitmap(originalCameraImage, rect.left,rect.top,rect.width(),rect.height());
                            ByteArrayOutputStream bos = new ByteArrayOutputStream();
                            bmCard.compress(Bitmap.CompressFormat.JPEG, 90, bos);
                            byte[] bitmapData = bos.toByteArray();

                            FileOutputStream fos = null;
                            try {
                                fos = new FileOutputStream(myUri.getPath());
                                fos.write(bitmapData);
                                fos.flush();
                                fos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    fos.close();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                bmCard.recycle();
                                Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                                scanIntent.setData(myUri);
                                sendBroadcast(scanIntent);

                                Intent intent = new Intent();
                                intent.setData(myUri);
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            originalCameraImage.recycle();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                }

                @Override
                public void onFeedBackMessage(@NonNull String s) {
                    switch (s) {
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_MOVE_PHONE_CENTER:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackCenterMessage) ? s : cameraScreenCustomization.feedBackCenterMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_MOVE_PHONE_AWAY:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackAwayMessage) ? s : cameraScreenCustomization.feedBackAwayMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_MOVE_PHONE_CLOSER:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackCloserMessage) ? s : cameraScreenCustomization.feedBackCloserMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_OPEN_EYES:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackOpenEyesMessage) ? s : cameraScreenCustomization.feedBackOpenEyesMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_FRAME_YOUR_FACE:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackframeMessage) ? s : cameraScreenCustomization.feedBackframeMessage;
                            break;
                        case FaceDetectionProcessor.ACCURA_FEEDBACK_MULTIPLE_FACES:
                            status = TextUtils.isEmpty(cameraScreenCustomization.feedBackMultipleFace) ? s : cameraScreenCustomization.feedBackMultipleFace;
                            break;

                    }
                    tv_status.setText(status);
                    status = s;
                    mHandler.sendEmptyMessageDelayed(1, 2000);
                }

                @Override
                public void onFailure(@NonNull Exception e) {

                }
            };

        return faceDetectionResultListener;
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                if (processor != null)
                    processor.setTakePicture(true);
            } else if (msg.what == 1) {
                tv_status.setText(TextUtils.isEmpty(cameraScreenCustomization.feedBackCenterMessage) ? FaceDetectionProcessor.ACCURA_FEEDBACK_MOVE_PHONE_CENTER : cameraScreenCustomization.feedBackCenterMessage);
            }

        }
    };

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private void startCameraSource() {
        if (cameraSource != null) {
            try {
                if (cameraPreview == null) {
                    Logger.e(TAG, "Preview is null");
                }
                cameraPreview.start(cameraSource, fireFaceOverlay);
            } catch (IOException e) {
//                Logger.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause() {
        super.onPause();
        cameraPreview.stop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (cameraSource != null) {
            cameraSource.release();
        }
    }

    private int dip2px(Context context, float dpValue) {
        try {
            final float scale = context.getResources().getDisplayMetrics().density;
            return (int) (dpValue * scale + 0.5f);
        } catch (Throwable throwable) {
            // igonre
        }
        return 0;
    }

    private String[] getRequiredPermissions() {
        return new String[]{Manifest.permission.CAMERA};
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                return false;
            }
        }
        return true;
    }

    private void getRuntimePermissions() {
        List<String> allNeededPermissions = new ArrayList<>();
        for (String permission : getRequiredPermissions()) {
            if (!isPermissionGranted(this, permission)) {
                allNeededPermissions.add(permission);
            }
        }

        if (!allNeededPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(
                    this, allNeededPermissions.toArray(new String[0]), PERMISSION_REQUESTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, @NonNull int[] grantResults) {
        if (allPermissionsGranted()) {
            createCameraSource();
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private boolean isPermissionGranted(Context context, String permission) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
