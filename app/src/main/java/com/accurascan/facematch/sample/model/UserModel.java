package com.accurascan.facematch.sample.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import androidx.annotation.Keep;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

@Keep
public class UserModel {

    int id;
    String userName;
    String userImage;
    float[] floatArray;


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFloatArrayAsString() {
        return encodeLocation(floatArray);
    }

    public void setFloatArrayAsString(String base64) {
        this.floatArray = decodeLocation(base64);
    }

    public float[] getFloatArray() {
        return floatArray;
    }

    public void setFloatArray(float[] floatArray) {
        this.floatArray = floatArray;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public Bitmap getUserBitmap(){
        if (this.userImage != null) {
            byte[] decodedString = Base64.decode(this.userImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        }
        return null;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }


    private static String encodeLocation(float[] floatArray) {
        return Base64.encodeToString(floatToByteArray(floatArray),Base64.DEFAULT);
    }
    private static float[] decodeLocation(String base64Encoded) {
        return byteToFloatArray(Base64.decode(base64Encoded,Base64.DEFAULT));
    }
    private static byte[] floatToByteArray(float[] floatArray) {
        ByteBuffer buf = ByteBuffer.allocate(Float.SIZE / Byte.SIZE * floatArray.length);
        buf.asFloatBuffer().put(floatArray);
        return buf.array();
    }
    private static float[] byteToFloatArray(byte[] bytes) {
        FloatBuffer buf = ByteBuffer.wrap(bytes).asFloatBuffer();
        float[] floatArray = new float[buf.limit()];
        buf.get(floatArray);
        return floatArray;
    }
}
