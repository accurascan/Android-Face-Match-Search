package com.inet.facelock.callback;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.Keep;

import com.accurascan.facematch.R;
import com.accurascan.facematch.util.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class FaceHelper extends FaceLockHelper{
    private Context context;
    public FaceDetectionResult leftResult = null;
    public FaceDetectionResult rightResult = null;
    public float match_score = 0.0f;
    public Bitmap face2 = null;
    public Bitmap face1 = null;
    private Activity activity;
    private FaceMatchCallBack faceMatchCallBack;
    private FaceCallback faceCallback;
//    private FaceLockHelper faceLockHelper;

    /**
     * override method to get face detect on input image and match image and
     * face match score between input and match image.
     *
     */
    @Keep
    public interface FaceMatchCallBack {

        /**
         * This is called after face match.
         * @param ret
         */
        void onFaceMatch(float ret);
        /**
         * This is called after face match.
         * @param ret
         * @param matchedPosition
         */
        void onFaceMatch(float ret,int matchedPosition);

        /**
         * This is callback function to get bitmap.
         * @param src
         */
        void onSetInputImage(Bitmap src);
        void onSetMatchImage(Bitmap src);

    }

    public FaceHelper() {
    }

    @Keep
    public FaceHelper(Activity activity) {
        this.context = activity.getApplicationContext();
        this.activity = activity;
        if (activity instanceof FaceCallback) {
            this.faceCallback = (FaceCallback) activity;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement " + FaceCallback.class.getName());
        }
        if (activity instanceof FaceMatchCallBack) {
            this.faceMatchCallBack = (FaceMatchCallBack) activity;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement "+ FaceMatchCallBack.class.getName());
        }
//        this.faceMatchCallBack = faceMatchCallBack;

        initEngine();
    }

    //initialize the engine
    @Keep
    public void initEngine() {
//        faceLockHelper = new FaceLockHelper();
        //call Sdk  method InitEngine
        // parameter to pass : FaceCallback callback, int fmin, int fmax, float resizeRate, String modelpath, String weightpath, AssetManager assets
        // this method will return the integer value
        //  the return value by initEngine used the identify the particular error
        // -1 - No key found
        // -2 - Invalid Key
        // -3 - Invalid Platform
        // -4 - Invalid License

        writeFileToPrivateStorage(R.raw.model, "model.prototxt"); //write file to private storage
        File modelFile = context.getFileStreamPath("model.prototxt");
        String pathModel = modelFile.getPath();
        writeFileToPrivateStorage(R.raw.weight, "weight.dat");
        File weightFile = context.getFileStreamPath("weight.dat");
        String pathWeight = weightFile.getPath();

        int nRet = /*faceLockHelper.*/InitEngine(this.faceCallback, 30, 800, 1.18f, pathModel, pathWeight, context.getAssets());
        if (nRet < 0) {
            AlertDialog.Builder builder1 = new AlertDialog.Builder(activity);
            if (nRet == -1) {
                builder1.setMessage("No Key Found");
            } else if (nRet == -2) {
                builder1.setMessage("Invalid Key");
            } else if (nRet == -3) {
                builder1.setMessage("Invalid Platform");
            } else if (nRet == -4) {
                builder1.setMessage("Invalid License");
            }

            builder1.setCancelable(true);

            builder1.setPositiveButton(
                    "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();

                        }
                    });

            AlertDialog alert11 = builder1.create();
            alert11.show();
        }
    }

    /**
     * This is the function to get Face match score.
     * pass two image uri to detect face and get face match score between two images.
     * and get data on {@link FaceMatchCallBack}
     *
     * @param uri1 to pass for detect face from image uri.
     * @param uri2 to pass for detect face from image uri.
     */
    @Keep
    public void getFaceMatchScore(Uri uri1, Uri uri2) {
        if (uri1 != null && uri2 != null) {
            getFaceMatchScore(FileUtils.getPath(activity, uri1), FileUtils.getPath(activity, uri2));
        } else {
            throw new NullPointerException("uri1 & uri2 cannot be null");
        }
    }

    /**
     * This is the function to get Face match score.
     * pass two image file to detect face and get face match score between two images.
     *
     * @param file1 to pass for detect face from image file.
     * @param file2 to pass for detect face from image file.
     */
    @Keep
    public void getFaceMatchScore(File file1, File file2) {
        if (file1 != null && file2 != null) {
            getFaceMatchScore(file1.getAbsolutePath(), file2.getAbsolutePath());
        } else {
            throw new NullPointerException("file1 & file2 cannot be null");
        }
    }

    /**
     * This is the function to get face from image file.
     *
     * @param inputFile pass image file to detect fce from image.
     */
    @Keep
    public void setInputImage(File inputFile) {
        if (inputFile != null) {
            setInputImage(inputFile.getAbsolutePath());
        } else {
            throw new NullPointerException("inputFile cannot be null");
        }
    }

    /**
     * This is the function to get face from image file uri.
     *
     * @param fileUri pass image uri to detect face from image.
     */
    @Keep
    public void setInputImage(Uri fileUri) {
        if (fileUri != null) {
            setInputImage(FileUtils.getPath(activity, fileUri));
        } else {
            throw new NullPointerException("fileUri cannot be null");
        }
    }

    /**
     * This is the function to get face from image path.
     *
     * @param inputPath pass image path to detect face from image.
     */
    @Keep
    public void setInputImage(String inputPath) {
        if (inputPath != null) {
            face1 = getBitmap(this.activity, inputPath);
            if (faceMatchCallBack != null) {
                if (face1 != null) {
                    faceMatchCallBack.onSetInputImage(face1);
                }
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement " + FaceMatchCallBack.class.getName());
            }
        } else {
            throw new NullPointerException("inputPath cannot be null");
        }
        leftResult = null;
//        if (face1 != null && face2 != null) {
        startFaceMatch();
//        }
    }

    @Keep
    public void setInputBase64(String base64){
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if (decodedByte != null) {
            setInputImage(decodedByte);
        } else {
            throw new RuntimeException("Invalid Image");
        }
    }

    /**
     * This is the function to get face from image path.
     *
     * @param bitmap pass bitmap to detect face from image.
     */
    @Keep
    public void setInputImage(Bitmap bitmap) {
        if (bitmap != null) {
            face1 = bitmap;
            if (faceMatchCallBack != null) {
                faceMatchCallBack.onSetInputImage(face1);
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement " + FaceMatchCallBack.class.getName());
            }
        } else {
            throw new NullPointerException("bitmap cannot be null");
        }
        leftResult = null;
//        if (face1 != null && face2 != null) {
        startFaceMatch();
//        }
    }



    /**
     * This is the function to get face from image path.
     *
     * to match face between two images then must have to call {@link FaceHelper#setInputImage(String)} and {@link FaceHelper#setMatchImage(String)}.
     *
     * @param matchFile pass image file
     */
    @Keep
    public void setMatchImage(File matchFile) {
        if (matchFile != null) {
            setMatchImage(matchFile.getAbsolutePath());
        } else {
            throw new NullPointerException("matchFile cannot be null");
        }
    }

    /**
     * This is the function to get face from image uri.
     *
     * to match face between two images then must have to call{@link FaceHelper#setInputImage(String)} and {@link FaceHelper#setMatchImage(String)}.
     *
     * @param fileUri pass image uri
     */
    @Keep
    public void setMatchImage(Uri fileUri) {
        if (fileUri != null) {
            setMatchImage(FileUtils.getPath(activity, fileUri));
        } else {
            throw new NullPointerException("fileUri cannot be null");
        }
    }

    /**
     * This is the function to get face from image uri.
     *
     * to match face between two images then must have to call {@link FaceHelper#setInputImage(String)} and {@link FaceHelper#setMatchImage(String)}.
     *
     * @param matchPath pass image path
     */
    @Keep
    public void setMatchImage(String matchPath) {
        if (face1 == null) {
            throw new RuntimeException(context.toString() + " Please set Input image First");
        }
        if (matchPath != null) {
            face2 = getBitmap(this.activity, matchPath);
            if (faceMatchCallBack != null) {
                if (face2 != null) {
                    faceMatchCallBack.onSetMatchImage(face2);
                }
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement " + FaceMatchCallBack.class.getName());

            }
        } else {
            throw new NullPointerException("matchPath cannot be null");
        }
        rightResult = null;
        startFaceMatch();
    }

    @Keep
    public FaceDetectionResult getFaceContent(String base64){
        byte[] decodedString = Base64.decode(base64, Base64.DEFAULT);
        Bitmap nBmp = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        if (nBmp != null) {
            int w = nBmp.getWidth();
            int h = nBmp.getHeight();
            int s = (w * 32 + 31) / 32 * 4;
            ByteBuffer buff = ByteBuffer.allocate(s * h);
            nBmp.copyPixelsToBuffer(buff);
            FaceDetectionResult faceDetectionResult = new FaceDetectionResult();
            int i = DetectRightFaceFeatures(buff.array(), w, h, null, faceDetectionResult);
            return i > 0 ? faceDetectionResult : null;
        } else {
            throw new RuntimeException("Invalid Image");
        }
    }

    /**
     * This is the function to get face from bitmap.
     *
     * to match face between two images then must have to call {@link FaceHelper#setInputImage(String)} and {@link FaceHelper#setMatchImage(String)}
     *
     * @param bitmap pass bitmap
     */
    @Keep
    public void setMatchImage(Bitmap bitmap) {
        if (face1 == null) {
            throw new RuntimeException(context.toString() + " Please set Input image First");
        }
        if (bitmap != null) {
            face2 = bitmap.copy(Bitmap.Config.ARGB_8888, false);
            if (faceMatchCallBack != null) {
                if (face2 != null) {
                    faceMatchCallBack.onSetMatchImage(face2);
                }
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement " + FaceMatchCallBack.class.getName());

            }
        } else {
            throw new NullPointerException("matchPath cannot be null");
        }
        rightResult = null;
        startFaceMatch();
    }

    @Keep
    public Bitmap getBitmap(Context activity, String path) {
        if (path != null) {
            Bitmap bmp1 = rotateImage(activity, path);
            if (bmp1 != null) {
                return bmp1.copy(Bitmap.Config.ARGB_8888, true);
            }
            return null;
        } else {
            throw new NullPointerException("path cannot be null");
        }
    }

    /**
     * This is the function to get Face match score.
     * pass two image path to detect face and get face match score between two images.
     *
     * @param path1 to pass for detect face from image path.
     * @param path2 to pass for detect face from image path.
     */
    private void getFaceMatchScore(String path1, String path2) {
        if (path1 != null && path2 != null) {
            face1 = getBitmap(this.activity, path1);
            face2 = getBitmap(this.activity, path2);
            if (faceMatchCallBack != null) {
                faceMatchCallBack.onSetInputImage(face1);
                faceMatchCallBack.onSetMatchImage(face2);
            } else {
                throw new RuntimeException(context.toString()
                        + " must implement " + FaceMatchCallBack.class.getName());
            }
            startFaceMatch();
        } else {
            throw new NullPointerException("path1 & path2 cannot be null");
        }
    }

    private void writeFileToPrivateStorage(int fromFile, String toFile) {
        InputStream is = context.getResources().openRawResource(fromFile);
        int bytes_read;
        byte[] buffer = new byte[4096];
        try {
            FileOutputStream fos = context.openFileOutput(toFile, Context.MODE_PRIVATE);

            while ((bytes_read = is.read(buffer)) != -1)
                fos.write(buffer, 0, bytes_read); // write

            fos.close();
            is.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This the function to detect face onLeftResult. and get face match score @onFaceMatch override method.
     * @param faceResult
     */
    @Keep
    public void onLeftDetect(FaceDetectionResult faceResult) {
        leftResult = null;
        if (faceResult != null) {
            leftResult = faceResult;

            if (face2 != null) {
                Bitmap nBmp = face2.copy(Bitmap.Config.ARGB_8888, true);
                if (nBmp != null && !nBmp.isRecycled()) {
//                    if (leftResult != null) {
//                        DetectRightFaceBase64(base64FromImage(nBmp), leftResult.getFeature());
//                    } else{
//                        DetectRightFaceBase64(base64FromImage(nBmp), null);
//                    }
                    int w = nBmp.getWidth();
                    int h = nBmp.getHeight();
                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);
                    if (leftResult != null) {
                        /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, leftResult.getFeature());
                    } else {
                        /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, null);
                    }
                }
            }
        } else {
            if (face2 != null) {
                Bitmap nBmp = face2.copy(Bitmap.Config.ARGB_8888, true);
                if (nBmp != null && !nBmp.isRecycled()) {
//                    if (leftResult != null) {
//                        DetectRightFaceBase64(base64FromImage(nBmp), leftResult.getFeature());
//                    } else{
//                        DetectRightFaceBase64(base64FromImage(nBmp), null);
//                    }
                    int w = nBmp.getWidth();
                    int h = nBmp.getHeight();
                    int s = (w * 32 + 31) / 32 * 4;
                    ByteBuffer buff = ByteBuffer.allocate(s * h);
                    nBmp.copyPixelsToBuffer(buff);
                    if (leftResult != null) {
                        /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, leftResult.getFeature());
                    } else {
                        /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, null);
                    }
                }
            }
        }
//        calcMatch();
    }

    /**
     * This the function to detect face onRightResult. and get face match score @onFaceMatch override method.
     * @param faceResult
     */
    @Keep
    public void onRightDetect(FaceDetectionResult faceResult) {
        if (faceResult != null) {
            rightResult = faceResult;
        } else {
            rightResult = null;
        }
        calcMatch(faceResult);
    }


    private void calcMatch(FaceDetectionResult rightResult) {
        if (leftResult == null || rightResult == null) {
            match_score = 0.0f;
        } else {
            match_score = /*faceLockHelper.*/Similarity(leftResult.getFeature(), rightResult.getFeature(), rightResult.getFeature().length);
            match_score *= 100.0f;
        }
        if (faceMatchCallBack != null) {
            faceMatchCallBack.onFaceMatch(match_score);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement com.inet.facelock.callback.FaceHelper.FaceMatchCallBack");
        }
    }

    @Keep
    public void recognizeFace(float[] inputData, float[][] feturesList) {
        int[] i = new int[2];
        if (inputData == null) {
            match_score = 0.0f;
        } else {
            match_score = /*faceLockHelper.*/MatchFace(inputData, feturesList, inputData.length, i);
            match_score *= 100.0f;
        }
        if (faceMatchCallBack != null) {
            faceMatchCallBack.onFaceMatch(match_score, i[0]);
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement com.inet.facelock.callback.FaceHelper.FaceMatchCallBack");
        }
    }

    private void startFaceMatch() {

        if (face1 != null && leftResult == null) {
            //Bitmap nBmp = RecogEngine.g_recogResult.faceBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Bitmap nBmp = face1.copy(Bitmap.Config.ARGB_8888, true);
//            DetectLeftFaceBase64(base64FromImage(nBmp));
//            DetectLeftFaceBase64("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");
            int w = nBmp.getWidth();
            int h = nBmp.getHeight();
            int s = (w * 32 + 31) / 32 * 4;
            ByteBuffer buff = ByteBuffer.allocate(s * h);
            nBmp.copyPixelsToBuffer(buff);
            /*faceLockHelper.*/DetectLeftFace(buff.array(), w, h);
        }

        if (face2 != null) {
            Bitmap nBmp = face2;
//            if (leftResult != null) {
//                DetectRightFaceBase64(base64FromImage(nBmp), leftResult.getFeature());
//            } else {
//                DetectRightFaceBase64(base64FromImage(nBmp), null);
//            }
            int w = nBmp.getWidth();
            int h = nBmp.getHeight();
            int s = (w * 32 + 31) / 32 * 4;
            ByteBuffer buff = ByteBuffer.allocate(s * h);
            nBmp.copyPixelsToBuffer(buff);
            if (leftResult != null) {
                /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, leftResult.getFeature());
            } else
                /*faceLockHelper.*/DetectRightFace(buff.array(), w, h, null);
        }
    }

    /**
     * return Bitmap from given image path.
     *
     *
     * @param activity
     * @param path
     * @return bitmap according to the orientation.
     */
    private Bitmap rotateImage(Context activity, final String path) {

        Bitmap b = decodeFileFromPath(activity, path);

        try {
            ExifInterface ei = new ExifInterface(path);
            int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            Matrix matrix = new Matrix();
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    matrix.postRotate(90);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);

                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    matrix.postRotate(180);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    matrix.postRotate(270);
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    break;
                default:
                    b = Bitmap.createBitmap(b, 0, 0, b.getWidth(), b.getHeight(), matrix, true);
                    //b.copyPixelsFromBuffer(ByteBuffer.)
                    break;
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return b;
    }

    /**
     * Decode an path into a bitmap.
     *
     *
     * @param activity
     * @param path imagepath is not null
     * @return Bitmap
     */
    private Bitmap decodeFileFromPath(Context activity, String path) {
        Uri uri = getImageUri(path);
        InputStream in = null;
        try {
            in = activity.getContentResolver().openInputStream(uri);

            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            BitmapFactory.decodeStream(in, null, o);
            in.close();

            int scale = 1;
            int inSampleSize = 1024;
            if (o.outHeight > inSampleSize || o.outWidth > inSampleSize) {
                scale = (int) Math.pow(2, (int) Math.round(Math.log(inSampleSize / (double) Math.max(o.outHeight, o.outWidth)) / Math.log(0.5)));
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            in = activity.getContentResolver().openInputStream(uri);
            // Bitmap b = BitmapFactory.decodeStream(in, null, o2);
            int MAXCAP_SIZE = 512;
            Bitmap b = getResizedBitmap(BitmapFactory.decodeStream(in, null, o2), MAXCAP_SIZE);
            in.close();

            return b;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a Uri from a path.
     *
     * @param path
     * @return a Uri for the given path
     */
    private Uri getImageUri(String path) {
        return Uri.fromFile(new File(path));
    }

    /**
     * Return resize bitmap
     *
     * @param image existing bitmap
     * @param maxSize maxSize is height or width according to bitmap ratio.
     * @return
     */
    private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 0) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    @Keep
    public void closeEngine(){
        /*faceLockHelper.*/CloseEngine();
    }

    private String base64FromImage(Bitmap bitmap){
//        return "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";
//        return "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";
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        return Base64.encodeToString(byteArray, Base64.NO_WRAP);

    }
}