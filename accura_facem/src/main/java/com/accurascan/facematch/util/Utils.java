package com.accurascan.facematch.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.accurascan.facematch.BuildConfig;
import com.accurascan.facematch.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static androidx.core.content.ContextCompat.checkSelfPermission;

public class Utils {

    public static boolean isPermissionsGranted(Context context) {
        for (String permission : getRequiredPermissions(context)) {
            if (checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private static String[] getRequiredPermissions(Context context) {
        String[] ps = new String[] {
                Manifest.permission.CAMERA,Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
//        try {
//            PackageInfo info =
//                    context
//                            .getPackageManager()
//                            .getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
//            String[] ps = info.requestedPermissions;
//            return (ps != null && ps.length > 0) ? ps : new String[0];
//        } catch (Exception e) {
//            return new String[0];
//        }

        return ps;
    }
}