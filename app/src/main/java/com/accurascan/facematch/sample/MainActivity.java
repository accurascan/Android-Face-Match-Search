package com.accurascan.facematch.sample;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.accurascan.facematch.sample.database.DatabaseHelper;
import com.accurascan.facematch.sample.model.UserModel;
import com.accurascan.facematch.ui.FaceMatchActivity;
import com.accurascan.facematch.util.Utils;
import com.inet.facelock.callback.FaceCallback;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

import java.io.File;
import java.text.NumberFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements FaceCallback, FaceHelper.FaceMatchCallBack {
    private static final int CAPTURE_IMAGE = 101;
    FaceHelper faceHelper;
    ImageView imLeft, imRight;
    TextView tv_match_score;
    private long start;
    private ProgressDialog progressBar;
    private List<UserModel> faceModelList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);
        faceHelper = new FaceHelper(this);
        imLeft = findViewById(R.id.im_left);
        imRight = findViewById(R.id.im_right);
        tv_match_score = findViewById(R.id.tv_match_score);

        if (!Utils.isPermissionsGranted(this)) {
            requestCameraPermission();
        }
    }


    //requesting the camera permission
    public void requestCameraPermission() {
        int currentapiVersion = Build.VERSION.SDK_INT;
        if (currentapiVersion >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);

                } else {
                    requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    requestCameraPermission();
                }
                return;
            }
        }
    }


    public void AccuraFaceMatch(View view) {
        startActivity(new Intent(this, FaceMatchActivity.class));
    }

    public void AddUser(View view) {
        startActivity(new Intent(this, UserListActivity.class));
    }


    public void AccuraLeftFace(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File f = new File(getCacheDir(), "temp.jpg");
        Uri uriForFile = FileProvider.getUriForFile(
                MainActivity.this,
                getPackageName() + ".provider",
                f
        );
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uriForFile);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            intent.putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_FRONT);
            intent.putExtra("android.intent.extras.LENS_FACING_FRONT", 1);
            intent.putExtra("android.intent.extra.USE_FRONT_CAMERA", true);
        } else {
            intent.putExtra("android.intent.extras.CAMERA_FACING", 1);
        }
        startActivityForResult(intent, CAPTURE_IMAGE);
    }

    public void AccuraRightFace(View view) {
        this.faceModelList = dbHelper.getAllUser();
    }

    @Override
    public void onInitEngine(int ret) {

    }

    /**
     * This method execute after {@link FaceHelper#setInputImage(Uri)} ()}
     *
     * @param face    face content
     */
    @Override
    public void onLeftDetect(FaceDetectionResult face) {
        start = System.currentTimeMillis();
        if (face != null) {
            faceHelper.recognizeFace(face.getFeature(), dbHelper.getFeatureList());
        }
    }

    @Override
    public void onRightDetect(FaceDetectionResult face) {
    }

    @Override
    public void onExtractInit(int ret) {

    }

    /**
     * This method execute after {@link FaceHelper#recognizeFace(float[], float[][])}
     *
     * @param score             Face match score
     * @param matchedPosition   matched position with database record
     */
    @Override
    public void onFaceMatch(float score, int matchedPosition) {
        final long durationInMili = System.currentTimeMillis() - start;
        final long l = TimeUnit.MILLISECONDS.toSeconds(durationInMili);
        NumberFormat nf = NumberFormat.getNumberInstance();
        nf.setMaximumFractionDigits(1);
        String ss = nf.format(score);
        Log.e(MainActivity.class.getSimpleName(), "Match Score : " + matchedPosition + "-> " + ss);
        faceModelList = dbHelper.getAllUser();
        if (score > 75) {
            UserModel matchedData = faceModelList.get(matchedPosition);
            tv_match_score.setText(String.format("Total time(milli second) : %d ms\nTotal time(seconds) : %d s\nNo. of Compared images : %d\nName : %s" +
                    "\nMatch Score : %s %%\n\nMATCHED FOUND", durationInMili, l, faceModelList.size(), matchedData.getUserName(), ss));

            if (matchedData.getUserBitmap() != null) {
                imRight.setImageBitmap(matchedData.getUserBitmap());
            }
        } else {
            tv_match_score.setText(String.format("Total time(milli second) : %d ms\nTotal time(seconds) : %d s\nNo. of Compared images : %d\nMax Score : %s %%" +
                    "\n\nNOT MATCHED", durationInMili, l, faceModelList.size(), ss));
            imRight.setImageBitmap(null);
        }

    }

    @Override
    public void onFaceMatch(float ret) {

    }

    /**
     * This method execute after {@link FaceHelper#setInputImage(Uri)} and all input image supported methods
     *
     * @param src    Input image as bitmap to display
     */
    @Override
    public void onSetInputImage(Bitmap src) {
        imLeft.setImageBitmap(src);
    }

    @Override
    public void onSetMatchImage(final Bitmap src) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE) { // handle request code CAPTURE_IMAGE used for capture image in camera
                File f = new File(getCacheDir(), "temp.jpg");

                if (!f.exists())
                    return;
                faceHelper.setInputImage(f.getAbsolutePath());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (faceHelper != null) {
            faceHelper.closeEngine();
        }
    }
}
