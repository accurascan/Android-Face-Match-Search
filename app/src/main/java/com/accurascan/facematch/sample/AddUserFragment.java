package com.accurascan.facematch.sample;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.accurascan.facematch.sample.database.DatabaseHelper;
import com.accurascan.facematch.sample.model.UserModel;
import com.inet.facelock.callback.FaceDetectionResult;
import com.inet.facelock.callback.FaceHelper;

import java.io.ByteArrayOutputStream;
import java.io.File;

import static android.app.Activity.RESULT_OK;

@Keep
public class AddUserFragment extends Fragment implements View.OnClickListener {
    private static final int CAPTURE_IMAGE = 101;
    Button btnAddUser,btnPick;
    EditText etUserName;
    UserListActivity mActivity;
    DatabaseHelper databaseHelper;
    Bitmap bitmap = null;
    private FaceHelper faceHelper;
    private ImageView imUser;
    private View lout;

    private static onFragmentInteractionListner mListner;

    public interface onFragmentInteractionListner {
        void onFragmentInteraction();
    }

    public AddUserFragment() {
    }

    public static AddUserFragment newInstance(onFragmentInteractionListner listner) {
        mListner = listner;
        return new AddUserFragment();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_form, container, false);
        mActivity = (UserListActivity) getActivity();
        databaseHelper = new DatabaseHelper(mActivity);
        btnAddUser = view.findViewById(R.id.btn_add);
        etUserName = view.findViewById(R.id.et_user_name);
        imUser = view.findViewById(R.id.im_user);
        lout = view.findViewById(R.id.lout_upload);
        btnPick = view.findViewById(R.id.btn_pick_image);

        btnAddUser.setOnClickListener(this);
        btnPick.setOnClickListener(this);
        return view;
    }

    public void setFaceHelper(FaceHelper faceHelper){
        this.faceHelper = faceHelper;
    }

    private String Base64FromBitmap(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_pick_image:
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                File f = new File(mActivity.getCacheDir(), "temp.jpg");
                Uri uriForFile = FileProvider.getUriForFile(
                        mActivity,
                        mActivity.getPackageName() + ".provider",
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
                break;
            case R.id.btn_add:
                if (bitmap == null) {
                    Toast.makeText(mActivity, "Image Not Picked", Toast.LENGTH_SHORT).show();
                    return;
                }
                String base64Image = Base64FromBitmap(bitmap);
                FaceDetectionResult faceContent = faceHelper.getFaceContent(base64Image);
                if (faceContent == null) {
                    Toast.makeText(mActivity, "Face not extracted", Toast.LENGTH_SHORT).show();
                    return;
                }
                String userName = etUserName.getText().toString();
                if (!TextUtils.isEmpty(userName) && !userName.isEmpty()) {

                    if (base64Image != null && !base64Image.isEmpty() && !base64Image.equalsIgnoreCase(" ")) {
                        UserModel userModel = new UserModel();
                        userModel.setUserName(userName);
                        userModel.setUserImage(base64Image);
                        userModel.setFloatArray(faceContent.getFeature());
                        databaseHelper.addUser(userModel);

                        etUserName.setText("");
                        etUserName.setHint(R.string.hint_user_name);
                        bitmap = null;
                        imUser.setImageDrawable(getResources().getDrawable(R.drawable.placeholder));
                        lout.setVisibility(View.VISIBLE);

                        mListner.onFragmentInteraction();
                        Toast.makeText(mActivity, "Record inserted successfully", Toast.LENGTH_SHORT).show();
                    }else {
                        Toast.makeText(mActivity, "Invalid Image", Toast.LENGTH_SHORT).show();
                    }
                }else {
                    Toast.makeText(mActivity, "Plz enter Name", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == CAPTURE_IMAGE) { // handle request code CAPTURE_IMAGE used for capture image in camera
                File f = new File(mActivity.getCacheDir(), "temp.jpg");

                if (!f.exists())
                    return;
                bitmap = faceHelper.getBitmap(mActivity,
                        f.getAbsolutePath());
                if (bitmap!=null) {
                    imUser.setImageBitmap(bitmap);
                    lout.setVisibility(View.GONE);
                }
            }
        }
    }
}
